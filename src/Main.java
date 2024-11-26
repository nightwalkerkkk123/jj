import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;/**
 * @author wangzihao
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner=new Scanner(System.in);
        String name=scanner.nextLine();
        appendToFile("input",name);
        int lineNumber = 2; // 要修改的行号
        String newContent = "这是新的内容。";
        int line=findLineNumber("input","rr");
        modifyLineInFile("input", line, newContent);
        readFile2();
    }
    public static void writeFile(String name) throws IOException {
       try(OutputStream output=new FileOutputStream("input")){
           output.write(name.getBytes(StandardCharsets.UTF_8));
       }
    }
    public static void appendToFile(String path,String data)
    {
        try(FileWriter writer=new FileWriter(path,true)){
            writer.write(data);
            writer.write(System.lineSeparator());
            System.out.println("数据成功添加！");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void readFile() throws IOException {
        String s;
        try(InputStream input=new FileInputStream("input")){
            int n;
            StringBuilder sb=new StringBuilder();
            while((n=input.read())!=-1){
                sb.append((char)n);
            }
            s=sb.toString();
        }
        System.out.println(s);
    }
    public static void readFile2() throws IOException {
        Reader reader=new FileReader("input");
        for (;;)
        {
            int n=reader.read();
            if (n==-1){ break;}
            System.out.print((char)n);
        }
        reader.close();
    }
    public static void modifyLineInFile(String filePath,int lineNumber,String newContent) {
        List<String> lines=new ArrayList<>();
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

                // 修改指定行
                if (lineNumber >= 1 && lineNumber <= lines.size()) {
                    lines.set(lineNumber - 1, newContent); // lineNumber从1开始
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
}