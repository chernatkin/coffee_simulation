package org.chernatkin.coffee.simulation.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.chernatkin.coffee.simulation.exception.OrderNotFoundException;
import org.chernatkin.coffee.simulation.exception.TooManyRequestsException;

public class CoffeeDao {
    
    public static final int MACHINES_COUNT = 2;
    
    private static final String ORDER_ID_COLUMN = "order_id";
    
    private static final String COFFEE_TYPE_COLUMN = "coffee_type";
    
    private static final String PAYMENT_TYPE_COLUMN = "payment_type";
    
    private static final String LAST_STEP_COLUMN = "step";
    
    private static final String COFFEE_MASHINE_NUMBER_COLUMN = "machine_id";
    
    private static final int PICK_FAVORITES_TIME = 500;
    
    private static final int FIND_CUP_TIME = 250;
    
    private static final int PUT_CUP_TIME = 250;
    
    private static final int PICK_TYPE_TIME = 250;
    
    private static final int LEAVE_TIME = 250;
    
    private static final int CONST_TIME = PICK_FAVORITES_TIME + FIND_CUP_TIME + PUT_CUP_TIME + PICK_TYPE_TIME + LEAVE_TIME;
    
    @Inject
    private DataSource dataSource;
    
    @PostConstruct
    //TODO: Remove this method if used db as dedicated process, not in memory db
    public void recreateDatabase() throws SQLException {
        final Connection conn = dataSource.getConnection();
        conn.createStatement().execute("DROP TABLE coffee_order IF EXISTS");
        
        conn.createStatement().execute("CREATE TABLE coffee_order ( "
                + ORDER_ID_COLUMN + " BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY, "
                + COFFEE_TYPE_COLUMN + " VARCHAR(32) NOT NULL, "
                + PAYMENT_TYPE_COLUMN + " VARCHAR(32) NULL, "
                + LAST_STEP_COLUMN + " VARCHAR(32) NOT NULL,"
                + COFFEE_MASHINE_NUMBER_COLUMN + " INTEGER"
                + ")");
        
        conn.close();
    }
    
    public CoffeeOrder pickFavorites(final CoffeeType coffeeType) throws SQLException {
        try(final Connection conn = dataSource.getConnection()) {
            
            final PreparedStatement ps = conn.prepareStatement("INSERT INTO coffee_order (coffee_type, step) VALUES (?, ?)", 
                                                               Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, coffeeType.name());
            ps.setString(2, OrderStep.PICK_FAVORITES.name());
            ps.executeUpdate();
            
            final ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            
            return new CoffeeOrder(rs.getLong(1), coffeeType, null, OrderStep.PICK_FAVORITES, null);
        }
    }
    
    public CoffeeOrder payCoffee(final long orderId, final PaymentType paymentType) throws SQLException, TooManyRequestsException {
        try(final Connection conn = dataSource.getConnection()) {
            
            executeUpdate(conn, "UPDATE coffee_order SET payment_type = ?, step = ? WHERE order_id = ?", paymentType.name(), OrderStep.PAID.name(), orderId);
            return loadOrder(conn, orderId);
        }
    }
    
    public CoffeeOrder assignMachine(final CoffeeOrder order, final int machineId) throws SQLException {
        try(final Connection conn = dataSource.getConnection()) {
            executeUpdate(conn, "UPDATE coffee_order SET machine_id = ?, step = ? WHERE order_id = ?", machineId, OrderStep.ASSIGNED_MACHINE.name(), order.getOrderId());
            order.setMachineId(machineId);
            order.setStep(OrderStep.ASSIGNED_MACHINE);
            return order;
        }
    }
    
    public CoffeeOrder coffeeDone(final CoffeeOrder order) throws SQLException{
        try(final Connection conn = dataSource.getConnection()) {
            executeUpdate(conn, "UPDATE coffee_order SET step = ? WHERE order_id = ?", OrderStep.COFFEE_DONE.name(), order.getOrderId());
            order.setStep(OrderStep.COFFEE_DONE);
            return order;
        }
    }
    
    public Map<String, Integer> getSoldStatsByPaymentType() throws SQLException {
        try(final Connection conn = dataSource.getConnection()) {
            final ResultSet rs = conn.prepareStatement("SELECT payment_type, COUNT(*) AS cup_count FROM coffee_order WHERE payment_type IS NOT NULL GROUP BY payment_type")
                                     .executeQuery();
            
            final Map<String, Integer> stats = new HashMap<>();
            while(rs.next()){
                stats.put(rs.getString(PAYMENT_TYPE_COLUMN), rs.getInt("cup_count"));
            }

            return stats;
        }
    }
    
    public Map<Integer, Map<String, Integer>> getDispencedStatsByCoffeeType() throws SQLException {
        try(final Connection conn = dataSource.getConnection()) {
            final ResultSet rs = conn.prepareStatement("SELECT machine_id, coffee_type, COUNT(*) AS cup_count "
                                                     + "FROM coffee_order "
                                                     + "WHERE machine_id IS NOT NULL AND step = '" + OrderStep.COFFEE_DONE.name() + "' "
                                                     + "GROUP BY machine_id, coffee_type")
                                     .executeQuery();
            
            final Map<Integer, Map<String, Integer>> stats = new HashMap<>();
            while(rs.next()){
                final int machineId = rs.getInt(COFFEE_MASHINE_NUMBER_COLUMN);
                final CoffeeType coffeeType = CoffeeType.valueOf(rs.getString(COFFEE_TYPE_COLUMN));
                final int count = rs.getInt("cup_count");
                
                stats.computeIfAbsent(machineId, k -> new HashMap<>()).put(coffeeType.name(), count);
            }

            return stats;
        }
    }
    
    public int getAvgSpendTime() throws SQLException {
        try(final Connection conn = dataSource.getConnection()) {
            final PreparedStatement ps = conn.prepareStatement("SELECT AVG(" + CONST_TIME
                                                               + " + (CASE coffee_type WHEN ? THEN CAST(? AS INTEGER) "
                                                                                   + " WHEN ? THEN CAST(? AS INTEGER) "
                                                                                   + " WHEN ? THEN CAST(? AS INTEGER) "
                                                                                   + " END) "
                                                               + " + (CASE payment_type WHEN ? THEN CAST(? AS INTEGER) "
                                                                                    + " WHEN ? THEN CAST(? AS INTEGER) "
                                                                                    + " END) "
                                                                     + ") AS time_avg "
                                                             + "FROM coffee_order WHERE step = ?");

            CoffeeType[] coffeeTypes = CoffeeType.values();
            for(int i = 1; i <= coffeeTypes.length; i++) {
                ps.setString(2*i - 1, coffeeTypes[i - 1].name());
                ps.setLong(2*i, coffeeTypes[i - 1].getTime());
            }
            
            PaymentType[] paymentTypes = PaymentType.values();
            for(int i = 1; i <= paymentTypes.length; i++) {
                ps.setString(coffeeTypes.length*2 + 2*i - 1, paymentTypes[i - 1].name());
                ps.setLong(coffeeTypes.length*2 + 2*i, paymentTypes[i - 1].getTime());
            }
            
            ps.setString(paymentTypes.length*2 + coffeeTypes.length*2 + 1, OrderStep.COFFEE_DONE.name());
            
            final ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt("time_avg");
        }
    }
    
    public int getMinSpendTime() throws SQLException {
        try(final Connection conn = dataSource.getConnection()) {
            final PreparedStatement ps = conn.prepareStatement("SELECT MIN(" + CONST_TIME
                                                               + " + (CASE coffee_type WHEN ? THEN CAST(? AS INTEGER) "
                                                                                   + " WHEN ? THEN CAST(? AS INTEGER) "
                                                                                   + " WHEN ? THEN CAST(? AS INTEGER) "
                                                                                   + " END) "
                                                               + " + (CASE payment_type WHEN ? THEN CAST(? AS INTEGER) "
                                                                                    + " WHEN ? THEN CAST(? AS INTEGER) "
                                                                                    + " END) "
                                                                     + ") AS time_min "
                                                             + "FROM coffee_order WHERE step = ?");

            CoffeeType[] coffeeTypes = CoffeeType.values();
            for(int i = 1; i <= coffeeTypes.length; i++) {
                ps.setString(2*i - 1, coffeeTypes[i - 1].name());
                ps.setLong(2*i, coffeeTypes[i - 1].getTime());
            }
            
            PaymentType[] paymentTypes = PaymentType.values();
            for(int i = 1; i <= paymentTypes.length; i++) {
                ps.setString(coffeeTypes.length*2 + 2*i - 1, paymentTypes[i - 1].name());
                ps.setLong(coffeeTypes.length*2 + 2*i, paymentTypes[i - 1].getTime());
            }
            
            ps.setString(paymentTypes.length*2 + coffeeTypes.length*2 + 1, OrderStep.COFFEE_DONE.name());
            
            final ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt("time_min");
        }
    }
    
    public int getMaxSpendTime() throws SQLException {
        try(final Connection conn = dataSource.getConnection()) {
            final PreparedStatement ps = conn.prepareStatement("SELECT MAX(" + CONST_TIME
                                                               + " + (CASE coffee_type WHEN ? THEN CAST(? AS INTEGER) "
                                                                                   + " WHEN ? THEN CAST(? AS INTEGER) "
                                                                                   + " WHEN ? THEN CAST(? AS INTEGER) "
                                                                                   + " END) "
                                                               + " + (CASE payment_type WHEN ? THEN CAST(? AS INTEGER) "
                                                                                    + " WHEN ? THEN CAST(? AS INTEGER) "
                                                                                    + " END) "
                                                                     + ") AS time_max "
                                                             + "FROM coffee_order WHERE step = ?");

            CoffeeType[] coffeeTypes = CoffeeType.values();
            for(int i = 1; i <= coffeeTypes.length; i++) {
                ps.setString(2*i - 1, coffeeTypes[i - 1].name());
                ps.setLong(2*i, coffeeTypes[i - 1].getTime());
            }
            
            PaymentType[] paymentTypes = PaymentType.values();
            for(int i = 1; i <= paymentTypes.length; i++) {
                ps.setString(coffeeTypes.length*2 + 2*i - 1, paymentTypes[i - 1].name());
                ps.setLong(coffeeTypes.length*2 + 2*i, paymentTypes[i - 1].getTime());
            }
            
            ps.setString(paymentTypes.length*2 + coffeeTypes.length*2 + 1, OrderStep.COFFEE_DONE.name());
            
            final ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt("time_max");
        }
    }
    
    public CoffeeOrder loadOrder(final long orderId) throws SQLException {
        try(final Connection conn = dataSource.getConnection()) {
            return loadOrder(conn, orderId);
        }
    }
    
    private CoffeeOrder loadOrder(final Connection conn, final long orderId) throws SQLException {
        final List<CoffeeOrder> orders = queryOrder(conn, "SELECT * FROM coffee_order WHERE order_id = ?", orderId);
        if(orders.isEmpty()){
            throw new OrderNotFoundException();
        }
        assert orders.size() == 1;
        return orders.get(0);
    }
    
    private int executeUpdate(final Connection conn, final String sql, Object... params) throws SQLException {
        return applyParams(conn.prepareStatement(sql), params).executeUpdate();
    }
    
    private List<CoffeeOrder> queryOrder(final Connection conn, String sql, Object... params) throws SQLException {
        final ResultSet rs = applyParams(conn.prepareStatement(sql), params).executeQuery();
        
        final List<CoffeeOrder> places = new ArrayList<>();
        while(rs.next()){
            final Integer machineId = Optional.ofNullable(rs.getObject(COFFEE_MASHINE_NUMBER_COLUMN))
                                              .map(v -> ((Number) v).intValue())
                                              .orElse(null);
            
            places.add(new CoffeeOrder(rs.getLong(ORDER_ID_COLUMN),
                                       CoffeeType.valueOf(rs.getString(COFFEE_TYPE_COLUMN)),
                                       PaymentType.valueOfNullable(rs.getString(PAYMENT_TYPE_COLUMN)),
                                       OrderStep.valueOf(rs.getString(LAST_STEP_COLUMN)),
                                       machineId));
        }
        
        rs.close();
        return places;
    }
    
    private PreparedStatement applyParams(final PreparedStatement ps, Object... params) throws SQLException {
        if(params == null) {
            return ps;
        }
        for(int i = 0; i < params.length; i++){
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }
}
