
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame implements Runnable {
    // 基本界面设计
    private JPanel jpl = new JPanel();
    private JMenuBar bar = new JMenuBar();
    private JMenu menu = new JMenu("在线用户列表：");
    private JPanel jpn = new JPanel();
    // 底部面板
    private JPanel jps = new JPanel();
    private ImageIcon icon = new ImageIcon("image/send.png");
    private JButton jbt1 = new JButton(icon); // 系统消息发送键
    // 群发消息输入栏
    private JTextField jtf = new JTextField(40);
    // 套接字
    private ServerSocket ss = null;
    private Socket socket = null;
    // 存放用户名id
    private ArrayList<String> userNames = new ArrayList<>();
    // 存放用户端线程
    private ArrayList<ChatThread> users = new ArrayList<>();
    // 存放用户列表
    DefaultListModel<String> dl = new DefaultListModel();
    private JList<String> userList = new JList<>(dl);// 显示对象列表并且允许用户选择一个或多个项的组件。单独的模型 ListModel 维护列表的内容。
    // 存放聊天室队列
    private ArrayList<Room> gameList = new ArrayList<>();
    private ArrayList<Room> liveList = new ArrayList<>();
    private ArrayList<Room> eventList = new ArrayList<>();
    private ArrayList<Room> studyList = new ArrayList<>();
    // 服务端初始界面
    public Server() throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        bar.add(menu);
        menu.setFont(new Font("SimSun", Font.BOLD, 16));
        bar.setBackground(new Color(203, 203, 203));
        userList.setFont(new Font("SimSun", Font.BOLD, 16));
        this.add(bar, "North");
        this.add(userList, "Center");
        this.add(jpl, "South");
        jtf.setColumns(35);
        jpl.setLayout(new BorderLayout());

        // north
        jpl.add(jpn, BorderLayout.NORTH);
        jpn.setBorder(new EmptyBorder(5, 0, 5, 0));
        jpn.setLayout(new BorderLayout());
        JLabel t = new JLabel("群发消息:");
        t.setFont(new Font("SimSun", Font.BOLD, 16));
        t.setBorder(new EmptyBorder(5, 5, 0, 0));
        t.setOpaque(true); // 将组件设置为不透明
        t.setBackground(new Color(203, 203, 203));
        jpn.add(t, BorderLayout.SOUTH);

        // south
        jpl.add(jps, BorderLayout.SOUTH);
        jps.setLayout(new FlowLayout());
        jps.add(jtf);
        jps.add(jbt1);

        this.setTitle("服务器端");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(450, 500);
        this.setLocation(300, 200);
        this.setVisible(true);

        // 小飞机按钮--监听系统发送消息
        jbt1.addMouseListener(new SendListener());
        // 监听端口
        ss = new ServerSocket(9999);
        // 开启监听线程
        new Thread(this).start();
    }

    // 监听收发信息，做出事件处理
    @Override
    public void run() {
        while (true) {
            try {
                socket = ss.accept(); // 死等函数，如果没有客户端请求连接，程序将一直等待并阻塞程序
                ChatThread ct = new ChatThread(socket);
                System.out.println("用户已连接服务端");
                users.add(ct);
                ct.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this, "服务器异常！");
                System.exit(0);
            }
        }
    }

    // 监听系统发送消息
    public class SendListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            System.out.println("服务端群发消息");
            sendMessage("SYSMSG" + "#" + jtf.getText());
            jtf.setText("");
        }
    }

    // 处理用户端的线程
    public class ChatThread extends Thread {
        private Socket s = null;
        private BufferedReader br = null;
        private PrintStream ps = null;
        public boolean canRun = true;
        private String nickName = null; // 新开线程用户名
        private String chooseRoomName = null;  // 接收的来自哪个聊天室的数据

        public ChatThread(Socket s) throws Exception {
            this.s = s;
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ps = new PrintStream(s.getOutputStream(), true, "UTF-8");
        }

        public void run() {
            while (canRun) {
                try {
                    String msg = br.readLine();
                    String[] str = msg.split("#"); // 以“#”为分隔符，将提示符提取出来
                    if(str[0].equals("REGISTER")){  // 用户进入广场，检验用户名id重复性
                        if (userNames.contains(str[1]))
                        {
                            // 如果用户名已存在，提示用户
                            ps.println("WARN#");
                            // 由于客户被强制关闭广场时，会删掉一次，故要先加上该名字
                            userNames.add(str[1]);
                            System.out.println("用户名已存在，服务器添加用户信息："+ str[1]);
                        }else{
                            userNames.add(str[1]);
                            System.out.println("服务器添加用户信息："+ str[1]);
                            // 将现有的聊天室信息发送过去
                            for (Room r:gameList){
                                String roomStrs = "COMPONENT#" + r.getPerName() + "#" + r.getName() + "#" + r.getTag() + "#" + r.getInfo();
                                sendMessage(roomStrs);
                            }
                            for (Room r:liveList){
                                String roomStrs = "COMPONENT#" + r.getPerName() + "#" + r.getName() + "#" + r.getTag() + "#" + r.getInfo();
                                sendMessage(roomStrs);
                            }
                            for (Room r:eventList){
                                String roomStrs = "COMPONENT#" + r.getPerName() + "#" + r.getName() + "#" + r.getTag() + "#" + r.getInfo();
                                sendMessage(roomStrs);
                            }
                            for (Room r:studyList){
                                String roomStrs = "COMPONENT#" + r.getPerName() + "#" + r.getName() + "#" + r.getTag() + "#" + r.getInfo();
                                sendMessage(roomStrs);
                            }
                        }

                    }
                    else if (str[0].equals("LOGIN")) { // 用户进入了某个聊天室--LOGIN#nickName#chooseRoomName
                        nickName = str[1];
                        chooseRoomName = str[2];
                        if (!dl.contains(nickName)) { // 如果是重复的，就不加入
                            dl.addElement(nickName + "#" + chooseRoomName);
                            userList.repaint();
                        }
                        // 将服务器存储的被选择聊天室里所有用户的名字发送过去--USER#username#chooseRoomName
                        ListModel<String> model = userList.getModel(); // JList
                        for (int i = 0; i < model.getSize(); i++) {
                            String[] loginInfo = model.getElementAt(i).split("#");
                            if(loginInfo[1].equals(chooseRoomName)){
                                sendMessage("USER#" + loginInfo[0] + "#" + loginInfo[1]);
                                System.out.println("USER#" + model.getElementAt(i));
                            }
                        }
                        sendMessage(msg);
                    } else if (str[0].equals("EXIT") || str[0].equals("REMOVE")) { // EXIT用户自愿退出聊天室 REMOVE用户被强迫离开
                        // EXIT#name#chooseRoomName
                        sendMessage(msg);

                        // 服务器界面的列表移除该用户#聊天室的记录信息
                        String nameStr = str[1] + "#" + str[2];
                        for (int i = 0; i < dl.getSize(); i++) {
                            String model = dl.getElementAt(i);
                            if(model.equals(nameStr))
                            {
                                dl.removeElement(model);
                                userList.repaint();
                                System.out.println("服务器删掉" + nameStr + "信息");
                                break;
                            }
                        }
                    }else if(str[0].equals("LOGOFF")){ // 用户离开广场-- LOGOFF#nickName#chooseRoomName
                        String receivedName = str[1];
                        userNames.remove(receivedName);
                        System.out.println("服务器用户列表删掉" + str[1] + "的信息");
                    } else if (str[0].equals("COMPONENT")) { // 转发新建的聊天室标签组件信息，让每个客户端接受并绘制组件
                        // 添加聊天室存储信息
                        Room newRoom = new Room();
                        newRoom.setPerName(str[1]);
                        newRoom.setName(str[2]);
                        newRoom.setTag(str[3]);
                        newRoom.setInfo(str[4]);

                        if(newRoom.getTag().equals("游戏"))
                            gameList.add(newRoom);
                        else if(newRoom.getTag().equals("生活")){
                            liveList.add(newRoom);
                        }else if(newRoom.getTag().equals("时事")){
                            eventList.add(newRoom);
                        }else if(newRoom.getTag().equals("学习")){
                            studyList.add(newRoom);
                        }
                        sendMessage(msg);
                        System.out.println("服务器端接受并转发新建的聊天室数据");
                    } else if (str[0].equals("MSG") || str[0].equals("PMSG")) {
                        sendMessage(msg);
                    } else if (str[0].equals("DELETE")) {  // DELETE#chooseRoom
                        List<String> toRemove = new ArrayList<>();
                        for (int i = 0; i < dl.getSize(); i++) {
                            String[] delInfo = dl.getElementAt(i).split("#");
                            if (delInfo[1].equals(str[1])) {
                                // 保存要删除的元素
                                toRemove.add(dl.getElementAt(i));
                            }
                        }
                        // 一次性删除要删除的元素
                        for (String item : toRemove) {
                            dl.removeElement(item);
                            System.out.println("要删除的元素为：" + item);
                        }
                        // 更新 JList 的数据模型
                        userList.setModel(dl);
                        userList.repaint();
                    }else if(str[0].equals("IMG") || str[0].equals("PriIMG")){  // IMG#nickName#
                        System.out.println("服务器接收图片数据");
                        sendMessage(msg);
                        System.out.println("服务器发送图片字节数据");
                    }

                } catch (Exception e) {
                }
            }
        }
    }

    // 服务端发送给所有客户端消息
    public void sendMessage(String msg) {
        for (ChatThread ct : users) {
            ct.ps.println(msg);
            System.out.println("服务端发送：" + msg);
        }
    }

    public static void main(String[] args) throws Exception {
        new Server();
    }

}