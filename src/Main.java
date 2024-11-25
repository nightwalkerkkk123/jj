import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/test";
    private static final String USER = "root";
    private static final String PASS = "wan1234.";

    public static void main(String[] args) {
        createTables();

        // 创建4个订单
        insertOrder(1, 100.00); // orderId 将会是 1
        insertOrder(2, 200.00); // orderId 将会是 2
        insertOrder(1, 300.00); // orderId 将会是 3
        insertOrder(3, 400.00); // orderId 将会是 4

        // 为每个订单创建2个订单明细
        // 订单1的明细
        insertOrderDetail(1, 101, 2, 25.00);
        insertOrderDetail(1, 102, 2, 25.00);

        // 订单2的明细
        insertOrderDetail(2, 103, 1, 100.00);
        insertOrderDetail(2, 104, 1, 100.00);

        // 订单3的明细
        insertOrderDetail(3, 105, 3, 50.00);
        insertOrderDetail(3, 106, 3, 50.00);

        // 订单4的明细
        insertOrderDetail(4, 107, 2, 100.00);
        insertOrderDetail(4, 108, 2, 100.00);

        // 查询所有订单和明细
        System.out.println("\n===== 查询所有订单和对应明细 =====");
        for (int i = 1; i <= 4; i++) {
            getOrderWithDetails(i);
        }

        // 更新订单和明细示例
        System.out.println("\n===== 更新订单1和其明细 =====");
        updateOrder(1, 150.00);
        updateOrderDetail(1, 3, 30.00); // 更新订单1的第一个明细

        // 删除示例
        System.out.println("\n===== 删除订单4及其明细 =====");
        deleteOrderWithDetails(4);

        // 最终查询剩余订单
        System.out.println("\n===== 最终订单状态 =====");
        for (int i = 1; i <= 3; i++) {  // 只查询剩余的订单1-3
            getOrderWithDetails(i);
        }
    }

    public static void createTables() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // 创建用户表
                String createUserTable = "CREATE TABLE IF NOT EXISTS Users (" +
                        "user_id INT PRIMARY KEY, " +
                        "username VARCHAR(50), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

                // 修改订单表，添加外键约束
                String createOrderTable = "CREATE TABLE IF NOT EXISTS Orders (" +
                        "order_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id INT, " +
                        "total_amount DECIMAL(10, 2), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (user_id) REFERENCES Users(user_id))";

                // OrderDetails表保持不变
                String createOrderDetailTable = "CREATE TABLE IF NOT EXISTS OrderDetails (" +
                        "order_detail_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "order_id INT, " +
                        "product_id INT, " +
                        "quantity INT, " +
                        "unit_price DECIMAL(10, 2), " +
                        "subtotal DECIMAL(10, 2), " +
                        "FOREIGN KEY (order_id) REFERENCES Orders(order_id))";

                try (PreparedStatement pstmt0 = conn.prepareStatement(createUserTable);
                     PreparedStatement pstmt1 = conn.prepareStatement(createOrderTable);
                     PreparedStatement pstmt2 = conn.prepareStatement(createOrderDetailTable)) {
                    pstmt0.executeUpdate();
                    pstmt1.executeUpdate();
                    pstmt2.executeUpdate();
                    System.out.println("Tables created successfully.");

                    // 插入测试用户数据
                    String insertUsers = "INSERT IGNORE INTO Users (user_id, username) VALUES (?, ?)";
                    try (PreparedStatement pstmtUser = conn.prepareStatement(insertUsers)) {
                        for (int i = 1; i <= 3; i++) {
                            pstmtUser.setInt(1, i);
                            pstmtUser.setString(2, "User" + i);
                            pstmtUser.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void insertOrder(int userId, double totalAmount) {
        String sql = "INSERT INTO Orders (user_id, total_amount) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, totalAmount);
            pstmt.executeUpdate();
            System.out.println("Order inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertOrderDetail(int orderId, int productId, int quantity, double unitPrice) {
        String sql = "INSERT INTO OrderDetails (order_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            double subtotal = quantity * unitPrice;
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, unitPrice);
            pstmt.setDouble(5, subtotal);
            pstmt.executeUpdate();
            System.out.println("Order detail inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateOrder(int orderId, double totalAmount) {
        String sql = "UPDATE Orders SET total_amount = ? WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, totalAmount);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
            System.out.println("Order updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateOrderDetail(int orderDetailId, int quantity, double unitPrice) {
        String sql = "UPDATE OrderDetails SET quantity = ?, unit_price = ?, subtotal = ? WHERE order_detail_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            double subtotal = quantity * unitPrice;
            pstmt.setInt(1, quantity);
            pstmt.setDouble(2, unitPrice);
            pstmt.setDouble(3, subtotal);
            pstmt.setInt(4, orderDetailId);
            pstmt.executeUpdate();
            System.out.println("Order detail updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteOrder(int orderId) {
        String sql = "DELETE FROM Orders WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.executeUpdate();
            System.out.println("Order deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteOrderDetail(int orderDetailId) {
        String sql = "DELETE FROM OrderDetails WHERE order_detail_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderDetailId);
            pstmt.executeUpdate();
            System.out.println("Order detail deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getOrder(int orderId) {
        String sql = "SELECT * FROM Orders WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println("Order ID: " + rs.getInt("order_id"));
                System.out.println("User ID: " + rs.getInt("user_id"));
                System.out.println("Total Amount: " + rs.getDouble("total_amount"));
                System.out.println("Created At: " + rs.getTimestamp("created_at"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getOrderDetail(int orderDetailId) {
        String sql = "SELECT * FROM OrderDetails WHERE order_detail_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderDetailId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println("Order Detail ID: " + rs.getInt("order_detail_id"));
                System.out.println("Order ID: " + rs.getInt("order_id"));
                System.out.println("Product ID: " + rs.getInt("product_id"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.println("Unit Price: " + rs.getDouble("unit_price"));
                System.out.println("Subtotal: " + rs.getDouble("subtotal"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void deleteOrderWithDetails(int orderId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 先删除订单明细
                String deleteDetailsSQL = "DELETE FROM OrderDetails WHERE order_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteDetailsSQL)) {
                    pstmt.setInt(1, orderId);
                    pstmt.executeUpdate();
                }

                // 再删除订单
                String deleteOrderSQL = "DELETE FROM Orders WHERE order_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteOrderSQL)) {
                    pstmt.setInt(1, orderId);
                    pstmt.executeUpdate();
                }

                conn.commit();
                System.out.println("订单及其明细删除成功");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getOrderWithDetails(int orderId) {
        String sql = "SELECT o.*, od.* FROM Orders o " +
                "LEFT JOIN OrderDetails od ON o.order_id = od.order_id " +
                "WHERE o.order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            boolean first = true;
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                if (first) {
                    System.out.println("\n订单信息:");
                    System.out.println("订单ID: " + rs.getInt("order_id"));
                    System.out.println("用户ID: " + rs.getInt("user_id"));
                    System.out.println("总金额: " + rs.getDouble("total_amount"));
                    System.out.println("创建时间: " + rs.getTimestamp("created_at"));
                    System.out.println("\n订单明细:");
                    first = false;
                }
                System.out.println("---");
                System.out.println("明细ID: " + rs.getInt("order_detail_id"));
                System.out.println("商品ID: " + rs.getInt("product_id"));
                System.out.println("数量: " + rs.getInt("quantity"));
                System.out.println("单价: " + rs.getDouble("unit_price"));
                System.out.println("小计: " + rs.getDouble("subtotal"));
            }
            if (hasData) {
                System.out.println("\n=======================================\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
