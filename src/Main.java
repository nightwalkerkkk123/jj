import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    private static final int MAX_ATTEMPTS = 3; // 最大尝试次数
    private static final long LOCKOUT_DURATION = 30000; // 锁定时间（毫秒）

    private static int attemptCount = 0;
    private static long lockoutEndTime = 0;

    public static void main(String[] args) {
        JFrame frame = new JFrame("登录注册界面");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3, 2));

        JLabel userLabel = new JLabel("用户名:");
        JTextField userText = new JTextField();
        JLabel passwordLabel = new JLabel("密码:");
        JPasswordField passwordText = new JPasswordField();

        JButton loginButton = new JButton("登录");
        JButton registerButton = new JButton("注册");

        frame.add(userLabel);
        frame.add(userText);
        frame.add(passwordLabel);
        frame.add(passwordText);
        frame.add(loginButton);
        frame.add(registerButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());
                try {
                    if (isUserAuthenticated(username, password)) {
                        JOptionPane.showMessageDialog(frame, "登录成功");
                    } else {
                        JOptionPane.showMessageDialog(frame, "用户名或密码错误");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "登录时发生错误");
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());
                if (isUsernameAvailable(username)) {
                    if (isValidUsername(username) && isValidPassword(password)) {
                        appendToFile("user.txt", username);
                        appendToFile("user.txt", password);
                        JOptionPane.showMessageDialog(frame, "注册成功");
                    } else {
                        JOptionPane.showMessageDialog(frame, "用户名或密码格式错误");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "用户名已存在");
                }
            }
        });

        frame.setVisible(true);
    }

    // 验证用户凭据
    private static boolean isUserAuthenticated(String username, String password) throws IOException {
        int usernameLine = findLineNumber("username", username);
        int passwordLine = findLineNumber("password", password);
        return usernameLine != -1 && passwordLine == usernameLine + 1;
    }

    // 记住用户名
    private static void rememberUsername(String username) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("lastUser.txt"))) {
            writer.write(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取上次登录的用户名
    private static String getLastUsername() {
        try (BufferedReader reader = new BufferedReader(new FileReader("lastUser.txt"))) {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    // 检查用户名是否可用
    private static boolean isUsernameAvailable(String username) {
        return findLineNumber("username", username) == -1;
    }

    // 将字符串写入文件
    public static void writeFile(String name) throws IOException {
        try (OutputStream output = new FileOutputStream(name)) {
            output.write(name.getBytes(StandardCharsets.UTF_8));
        }
    }

    // 追加数据到文件
    public static void appendToFile(String path, String data) {
        try (FileWriter writer = new FileWriter(path, true)) {
            writer.write(data);
            writer.write(System.lineSeparator());
            System.out.println("数据成功添加！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取文件内容并打印
    public static void readFile() throws IOException {
        String s;
        try (InputStream input = new FileInputStream("input")) {
            int n;
            StringBuilder sb = new StringBuilder();
            while ((n = input.read()) != -1) {
                sb.append((char) n);
            }
            s = sb.toString();
        }
        System.out.println(s);
    }

    // 逐字符读取文件内容并打印
    public static void readFile2() throws IOException {
        try (Reader reader = new FileReader("input")) {
            for (;;) {
                int n = reader.read();
                if (n == -1) { break; }
                System.out.print((char) n);
            }
        } // try-with-resources 自动关闭 reader
    }

    // 修改文件中指定行的内容
    public static void modifyLineInFile(String filePath, int lineNumber, String newContent) {
        java.util.List<String> lines = new ArrayList<>();
        // 读取文件内容
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 修改或删除指定行
        if (lineNumber >= 1 && lineNumber <= lines.size()) {
            if (newContent == null) {
                lines.remove(lineNumber - 1); // 删除行
            } else {
                lines.set(lineNumber - 1, newContent); // 修改行
            }
        } else {
            System.out.println("行号超出范围。");
            return;
        }

        // 将修改后的内容写回文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("文件已更新。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 查找文件中指定内容的行号
    public static int findLineNumber(String filePath, String targetContent) {
        int lineNumber = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.equals(targetContent)) {
                    return lineNumber; // 返回匹配的行号
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1; // 如果没有找到匹配的行，返回-1
    }

    // 邮箱和手机号码的正则表达式
    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static final String PHONE_REGEX = "^\\+?[0-9]{10,15}$"; // 支持国际格式

    // 验证用户名是否合法
    public static boolean isValidUsername(String username) {
        // 检查长度要求
        if (username.length() < 5 || username.length() > 20) {
            return false;
        }

        // 验证是否为邮箱或手机号码
        boolean isEmail = Pattern.matches(EMAIL_REGEX, username);
        boolean isPhone = Pattern.matches(PHONE_REGEX, username);

        return isEmail || isPhone;
    }

    // 密码的正则表达式

    // 验证密码是否合法
    public static boolean isValidPassword(String password) {
        return password.length() >= 6 && password.length() <= 20;
    }
}