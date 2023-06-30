
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Client extends JFrame implements Runnable{
    // 套接字
    private Socket s;
    private BufferedReader br = null;
    private PrintStream ps = null;
    private Base64Encode Base64 = new Base64Encode();
    // 客户端用户名
    private String nickName;
    // 客户端广场界面
    private Square sq;
    // 客户端聊天室对象
    private ChatRoom ct;
    // 客户端存储聊天室的容器
    private JPanel gameJpl = new JPanel();
    private JPanel liveJpl = new JPanel();
    private JPanel eventJpl = new JPanel();
    private JPanel studyJpl = new JPanel();
    // 每个区的队列情况
    private ArrayList<Room> gameList = new ArrayList<>();
    private ArrayList<Room> liveList = new ArrayList<>();
    private ArrayList<Room> eventList = new ArrayList<>();
    private ArrayList<Room> studyList = new ArrayList<>();

    // 要进入并且监听的聊天室窗口
    private String chooseRoomName;
    private Room chooseRoom;

    public Client() throws Exception{
        s = new Socket("127.0.0.1", 9999);
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        ps = new PrintStream(s.getOutputStream(), true, "UTF-8");
        // 新建广场界面
        sq = new Square();
        // 启动监听线程
        new Thread(this).start();
        System.out.println(nickName + "进入广场");
    }

    public class Square extends JFrame implements ActionListener{
        // 面板设置
        private JFrame frame;
        private JTabbedPane tabbedPane = new JTabbedPane();
        private JPanel north = new JPanel();
        public JPanel south = new JPanel();

        // 创建聊天室的功能按钮
        public JButton jbt; // 创建聊天室按钮
        private JTextField nameField; // 聊天室名称组件
        private ButtonGroup group; // 聊天室标签单选按钮组
        private JRadioButton gameButton, lifeButton, currentEventsButton, sportsButton;
        private JTextArea introArea; // 聊天室简介组件
        private JButton confirmButton;// 确认创建聊天室按钮
        // 聊天室属性
        private String name; // 名称
        private String tag;  // 标签
        private String info; // 简介

        /** 初始化广场页面 **/
        public Square() {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            nickName = JOptionPane.showInputDialog("您的用户名：");
            this.setTitle(nickName + "的广场天地");
            ps.println("REGISTER#" + nickName);// **进入广场就发送注册消息

            this.pack();
            this.setSize(800, 600);
            this.setLocation(100, 100);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setLayout(new BorderLayout());

            // 频道面板
            this.add(north, BorderLayout.NORTH);
            this.add(south, BorderLayout.CENTER);
            north.setBackground(new Color(237, 222, 139));
            north.setLayout(new BorderLayout());
            JLabel jl = new JLabel("频道");
            jl.setFont(new Font("SimSun", Font.BOLD, 36));
            jl.setBorder(new EmptyBorder(20,20,20,0));
            north.add(jl, BorderLayout.WEST);
            // 创建按钮
            JPanel bottom = new JPanel();
            bottom.setBackground(new Color(237, 222, 139));
            jbt = new JButton("创建");
            jbt.setFont(new Font("SimSun", Font.BOLD, 16));
            bottom.add(jbt, BorderLayout.CENTER);
            north.add(bottom, BorderLayout.EAST);

            // 分区管理
            gameJpl.setPreferredSize(new Dimension(800, 500));
            liveJpl.setPreferredSize(new Dimension(800, 500));
            eventJpl.setPreferredSize(new Dimension(800, 500));
            studyJpl.setPreferredSize(new Dimension(800, 500));
            gameJpl.setBackground(new Color(237, 222, 139));
            liveJpl.setBackground(new Color(237, 222, 139));
            eventJpl.setBackground(new Color(237, 222, 139));
            studyJpl.setBackground(new Color(237, 222, 139));

            tabbedPane.add("游戏区", gameJpl);
            tabbedPane.add("生活区", liveJpl);
            tabbedPane.add("时事区", eventJpl);
            tabbedPane.add("学习区", studyJpl);

            south.add(tabbedPane);
            south.setBackground(new Color(251, 178, 23));

            studyJpl.setLayout(new FlowLayout(FlowLayout.LEFT));
            gameJpl.setLayout(new FlowLayout(FlowLayout.LEFT));
            liveJpl.setLayout(new FlowLayout(FlowLayout.LEFT));
            eventJpl.setLayout(new FlowLayout(FlowLayout.LEFT));

            this.setVisible(true);

            // 为广场上的"创建"按钮添加监听器
            jbt.addActionListener(this);

            // 重写窗口关闭的监听--关闭此窗口时删除用户信息
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    ps.println("LOGOFF#" + nickName + "#" + chooseRoomName);
                    System.out.println("LOGOFF#" + nickName + "#" + chooseRoomName);
                    System.exit(0);
                }
            });

        }

        /** ”创建“聊天室的小面板 **/
        public void createChatRoom() {
            frame = new JFrame("创建新的聊天室");
            // 创建主面板
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());

            // 创建名称文本框
            JPanel namePanel = new JPanel();
            namePanel.setBorder(new EmptyBorder(10, 0, 0, 0));
            namePanel.add(new JLabel("名称："));
            nameField = new JTextField(34);
            namePanel.add(nameField);
            panel.add(namePanel);

            // 创建标签下拉列表
            JPanel tagPanel = new JPanel();
            tagPanel.add(new JLabel("标签："));
            group = new ButtonGroup(); // 创建单选按钮组
            gameButton = new JRadioButton("游戏");
            lifeButton = new JRadioButton("生活");
            currentEventsButton = new JRadioButton("时事");
            sportsButton = new JRadioButton("学习");
            group.add(gameButton); // 将单选按钮添加到组中
            group.add(lifeButton);
            group.add(currentEventsButton);
            group.add(sportsButton);
            tagPanel.add(gameButton);
            tagPanel.add(lifeButton);
            tagPanel.add(currentEventsButton);
            tagPanel.add(sportsButton);
            tagPanel.setBorder(new EmptyBorder(0, 0, 8, 0));
            panel.add(tagPanel);

            // 创建简介文本框
            JPanel introPanel = new JPanel();
            panel.add(new JLabel("聊天室简介："));
            // 创建一个边框
            Border border = BorderFactory.createLineBorder(Color.BLACK);
            introArea = new JTextArea(5, 35);
            // 将边框设置到文本区域
            introArea
                    .setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            introPanel.add(introArea);
            introPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
            panel.add(introPanel);

            // 创建确认按钮
            confirmButton = new JButton("确认");
            confirmButton.addActionListener(this);
            panel.add(confirmButton);

            // 将主面板添加到窗口中央位置
            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            frame.setSize(300, 330);
            frame.setLocation(200, 200);
            frame.setVisible(true);
        }

        /** 在广场界面画出新的聊天室 **/
        public void drawChatRoom(JPanel jpl, String tag, String name, String info){
            JLabel label = new JLabel(name);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setPreferredSize(new Dimension(100, 40));
            // 创建一个内部边框（黑色实线）
            Border innerBorder = BorderFactory.createLineBorder(Color.BLACK);
            // 创建一个外部边框（10像素空白区域）
            Border outerBorder = BorderFactory.createEmptyBorder(10, 10, 0, 0);
            // 将内部边框和外部边框合并成一个复合边框
            Border compoundBorder = BorderFactory.createCompoundBorder(outerBorder, innerBorder);
            label.setBorder(compoundBorder);
            // 创建时增加监听
            label.addMouseListener(new RoomLabelListener());
            jpl.add(label);
            jpl.revalidate();
            jpl.repaint();
        }

        /** 在广场界面找到并移除聊天室 **/
        public void removeChatRoom(String roomName) { // 依次从4个面板里找要删的聊天室名字
            System.out.println("移除中");
            Component[] components = gameJpl.getComponents();
            for (Component component : components) {
                if (component instanceof JLabel) {
                    JLabel label = (JLabel) component;
                    if (label.getText().equals(roomName)) {
                        gameJpl.remove(label);
                    }
                }
            }
            gameJpl.revalidate();
            gameJpl.repaint();

            Component[] components1 = liveJpl.getComponents();
            for (Component component : components1) {
                if (component instanceof JLabel) {
                    JLabel label = (JLabel) component;
                    if (label.getText().equals(roomName)) {
                        liveJpl.remove(label);
                    }
                }
            }
            liveJpl.revalidate();
            liveJpl.repaint();

            Component[] components2 = eventJpl.getComponents();
            for (Component component : components2) {
                if (component instanceof JLabel) {
                    JLabel label = (JLabel) component;
                    if (label.getText().equals(roomName)) {
                        eventJpl.remove(label);
                    }
                }
            }
            eventJpl.revalidate();
            eventJpl.repaint();

            Component[] components3 = studyJpl.getComponents();
            for (Component component : components3) {
                if (component instanceof JLabel) {
                    JLabel label = (JLabel) component;
                    if (label.getText().equals(roomName)) {
                        studyJpl.remove(label);
                    }
                }
            }
            studyJpl.revalidate();
            studyJpl.repaint();

        }

        /** 根据聊天室名称判断是否重复 **/
        public boolean equals(String name){
            for(Room room : gameList){
                if(room.getName().equals(name))
                    return true;
            }
            for(Room room : liveList){
                if(room.getName().equals(name)){
                    return true;
                }
            }
            for(Room room : eventList){
                if(room.getName().equals(name)){
                    return true;
                }
            }
            for(Room room : studyList){
                if(room.getName().equals(name)){
                    return true;
                }
            }
            return false;
        }

        /** 根据聊天室名称寻找Room对象，以得到聊天室完整信息 **/
        public Room getRoom(String name){
            for(Room room : gameList){
                if(room.getName().equals(name)){
                    return room;
                }
            }
            for(Room room : liveList){
                if(room.getName().equals(name)){
                    return room;
                }
            }
            for(Room room : eventList){
                if(room.getName().equals(name)){
                    return room;
                }
            }
            for(Room room : studyList){
                if(room.getName().equals(name)){
                    return room;
                }
            }
            return null;
        }

        /** 监听广场界面的响应函数 **/
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == jbt) { // 广场上的"创建"按钮
                createChatRoom();
            } else if (e.getSource() == confirmButton) {// 创建小面板上的“确认”按钮
                this.info = introArea.getText();
                this.name = nameField.getText();
                // 判断此聊天室的名称是否和已存在的聊天室名称有重复
                if(equals(this.name))
                {
                    // 重复的话，发出警告
                    javax.swing.JOptionPane.showMessageDialog(null, "此房间名已被占用！");
                    return;
                }
                if (gameButton.isSelected()) {
                    this.tag = gameButton.getText();
                } else if (lifeButton.isSelected()) {
                    this.tag = lifeButton.getText();
                } else if (currentEventsButton.isSelected()) {
                    this.tag = currentEventsButton.getText();
                } else if (sportsButton.isSelected()) {
                    this.tag = sportsButton.getText();
                }
                System.out.println(this.tag);

                // 根据便签信息加到相应的Room对象队列中
                if(this.tag.equals("游戏")){
                    Room r = new Room(name, nickName, tag, info);
                    gameList.add(r);
                    drawChatRoom(gameJpl , tag, name, info);
                }else if(this.tag.equals("生活")){
                    Room r = new Room(name, nickName, tag, info);
                    liveList.add(r);
                    drawChatRoom(liveJpl , tag, name, info);
                }else if(this.tag.equals("时事")){
                    Room r = new Room(name, nickName, tag, info);
                    eventList.add(r);
                    drawChatRoom(eventJpl , tag, name, info);
                }else if(this.tag.equals("学习")){
                    Room r = new Room(name, nickName, tag, info);
                    studyList.add(r);
                    drawChatRoom(studyJpl , tag, name, info);
                }
                info = introArea.getText();
                frame.setVisible(false);
                System.out.println("客户端已发送COMPONENT#" + nickName + "#" + name + "#" + tag + "#" + info);
                ps.println("COMPONENT#" + nickName + "#" + name + "#" + tag + "#" + info); // 发送组件信息，让所有用户的广场上更新添加该标签
            }
        }

        /** 关闭广场界面 **/
        public void closeWindow() {
            this.dispose();
            // 表示用户注销
            ps.println("LOGOFF#" + nickName + "#" + chooseRoomName);
            System.out.println("LOGOFF#" + nickName + "#" + chooseRoomName);
        }

    }

    /** 点击聊天室，就会监听到并打开聊天室窗口 **/
    public class RoomLabelListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            System.out.println("进入监听函数");
            JLabel label = (JLabel) e.getSource();
            chooseRoomName = label.getText();
            System.out.println("监听的聊天室为:"+chooseRoomName);
            // 确定选择的聊天室
            chooseRoom = sq.getRoom(chooseRoomName);
            ct = new ChatRoom();
            System.out.println("用户：" + nickName + "已上线");
            // 发送用户上线信息
            ps.println("LOGIN#" + nickName + "#" + chooseRoomName);
            // 自动关闭广场窗口
            sq.setVisible(false);
        }
    }

    /** 进入群聊聊天室窗口 **/
    public class ChatRoom{
        // north:菜单栏
        private JMenuBar bar = new JMenuBar();
        private JMenu menu = null;
        private JFrame frame = null;

        // 公共聊天室的设置
        private JTextPane jta = null;
        private StyledDocument doc = null;

        private DefaultListModel<String> dl = new DefaultListModel<>();// 用来修改JList
        private JList<String> userList = new JList<>(dl);// 用来展示和选择

        // east:好友列表
        private JPanel east = new JPanel();
        private JScrollPane listPane = new JScrollPane(userList);

        // center:消息的主题面板
        private JPanel center = new JPanel();
        private JScrollPane js = null;
        private JPanel operPane = new JPanel();// 发送消息的操作面板
        private JLabel input = new JLabel("输入:");
        private JTextField jtf = new JTextField(47);
        private ImageIcon icon = new ImageIcon("image/send.png");
        private ImageIcon icon1 = new ImageIcon("image/Img.png");
        private JButton sendBtn = new JButton(icon);
        private JButton sendImg = new JButton(icon1);

        // 私聊和踢人
        private String Peruser = new String(); // 好友列表中选中的好友
        private String sender = null; // 私聊发送者的名字
        private String receiver = null; // 私聊接收者的名字
        private JButton reqBtn = null; // 私聊消息发送按钮
        private JButton tikBtn = null; // 踢人按钮
        private JPanel jps = null;

        // PriWindow:私聊窗口内的组件
        private JFrame jFrame;
        private JTextField jtf1 = new JTextField(45);
        private JTextPane priTextArea = null;
        private StyledDocument priDoc = null;
        private ImageIcon priIcon = new ImageIcon("image/send.png");
        private ImageIcon priIcon1 = new ImageIcon("image/Img.png");
        private JButton priSendBtn = new JButton(priIcon);
        private JButton priSendImg = new JButton(priIcon1);
        private JScrollPane js1 = null;
        private boolean openPriWin = false; // 此用户端的私聊窗口是否打开
        private boolean offLine = false;  // 标记此用户已经离开聊天室

        // 初始化群聊聊天室窗口
        public ChatRoom(){
            offLine = true;
            System.out.println("进入聊天室");
            frame = new JFrame(nickName + "---" + chooseRoom.getName());
            frame.setLayout(new BorderLayout());
            // north
            menu = new JMenu("房间简介:" + chooseRoom.getInfo());
            bar.add(menu);
            frame.add(bar, BorderLayout.NORTH);
            // east
            east.setPreferredSize(new Dimension(100, 150));// 在使用了布局管理器后用setPreferredSize来设置窗口大小
            east.setLayout(new BorderLayout());
            JLabel text = new JLabel("成员列表：");
            text.setFont(new Font("SimSun", Font.BOLD, 15));

            reqBtn = new JButton("私聊");
            tikBtn = new JButton("踢出");
            jps = new JPanel();
            reqBtn.setBorder(new EmptyBorder(0, 10, 0, 0));
            reqBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            tikBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            reqBtn.setHorizontalAlignment(JLabel.CENTER); // 设置水平居中
            reqBtn.setVerticalAlignment(JLabel.CENTER); // 设置垂直居中
            tikBtn.setHorizontalAlignment(JLabel.CENTER); // 设置水平居中
            tikBtn.setVerticalAlignment(JLabel.CENTER); // 设置垂直居中
            reqBtn.setPreferredSize(new Dimension(42, 25));
            tikBtn.setPreferredSize(new Dimension(42, 25));
            jps.add(reqBtn);
            jps.add(tikBtn);
            east.add(jps, BorderLayout.SOUTH);
            east.add(text, BorderLayout.NORTH);
            east.add(listPane, BorderLayout.CENTER);// 显示好友列表
            frame.add(east, BorderLayout.EAST);
            userList.setFont(new Font("KaiTi", Font.BOLD, 14));
            // center
            jta = new JTextPane();
            doc = jta.getStyledDocument();
            jta.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            jta.setFont(new Font("SimSun", Font.PLAIN, 14));
            jta.setEditable(false);
            js = new JScrollPane(jta);
            center.setLayout(new BorderLayout());

            // 流式布局
            FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
            operPane.setLayout(fl);
            operPane.add(input);
            operPane.add(jtf);
            operPane.add(sendBtn);
            operPane.add(sendImg);

            center.add(js, BorderLayout.CENTER);// js是消息展示框JScrollPane
            center.add(operPane, BorderLayout.SOUTH);
            frame.add(center, BorderLayout.CENTER);

            js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);// 需要时才显示滚动条
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(600, 500);
            frame.setLocation(300, 200);
            frame.setVisible(true);

            // 监听窗口函数
            frame.addWindowListener(new MyWindowAdapter()); // 群聊窗口关闭
            sendBtn.addActionListener(new SendActionListener()); // 发送文字消息
            sendImg.addActionListener(new SendImageListener()); // 发送图片
            reqBtn.addActionListener(new PersonActionListener()); // 私聊按钮
            tikBtn.addActionListener(new TikActionListener()); // 踢人按钮
        }

        // 构造私聊窗口
        public void PriWindow(String name) { // 建立私聊窗口
            jFrame = new JFrame();// 新建了一个窗口
            jFrame.setSize(380, 370);

            // 计算新窗口的位置
            int x = frame.getX() + (frame.getWidth() - jFrame.getWidth()) / 2;
            int y = frame.getY() + (frame.getHeight() - jFrame.getHeight()) / 2;
            jFrame.setLocation(x, y);

            jFrame.setTitle("与" + name + "对话中");
            JPanel JPL = new JPanel();
            JPanel JPL2 = new JPanel();
            // 底部导航栏
            JPL.setLayout(new FlowLayout(FlowLayout.LEFT));
            jtf1 = new JTextField(40);
            JPL.add(jtf1);
            JPL.add(priSendBtn);
            JPL.add(priSendImg);
            priTextArea = new JTextPane();
            priDoc = priTextArea.getStyledDocument(); // 富文本文档--可以显示颜色、大小、字体等
            priTextArea.setEditable(false);
            js1 = new JScrollPane(priTextArea);
            js1.setPreferredSize(new Dimension(350, 280));  // 设置滚动面板的首选大小
            //            js1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);// 需要时才显示滚动条
            JPL2.add(js1, BorderLayout.CENTER);
            JPL2.add(JPL, BorderLayout.SOUTH);
            jFrame.add(JPL2);
            jtf.setFont(new Font("宋体", Font.PLAIN, 15));
            jFrame.setVisible(true);
            jFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jtf1.setFocusable(true);// 设置焦点

            // 添加事件监听--监听私聊窗口信息发送
            priSendBtn.addActionListener(new PriSendListener());
            priSendImg.addActionListener(new SendPriImageListener());

            // 添加事件监听--监听私聊窗口关闭
            jFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    openPriWin = false;   // 标识私聊窗口关闭
                    jtf.setText("");
                    System.out.println(name + "退出私聊");
                }
            });
        }


        //事件监听类：
        // 群聊窗口关闭，提示"下线"
        public class MyWindowAdapter extends WindowAdapter {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println(nickName + "客户端发送下线信息");
                System.out.println("EXIT#" + nickName + "#" + chooseRoomName);
                ps.println("EXIT#" + nickName + "#" + chooseRoomName);
                sq.setVisible(true);
                offLine = false;
            }
        }

        // 监听"踢人"信息
        public class TikActionListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!userList.isSelectionEmpty()) { // 好友列表选中一个好友
                    // 如果这个用户端是管理员端口，才可以进行踢人操作
                    System.out.println("此房间的管理员是" + chooseRoom.getPerName());
                    System.out.println("进行踢人操作的用户是" + nickName);
                    if (nickName.equals(chooseRoom.getPerName())) {
                        // 如果此用户端是管理员
                        System.out.println(nickName + "正在进行踢人操作!");
                        Peruser = userList.getSelectedValuesList().get(0);// 获得被选择的用户
                        if (Peruser.equals(nickName + "(管理员)")) {
                            // 管理员不能踢自己
                            javax.swing.JOptionPane.showMessageDialog(frame, "注意，管理员正在进行违法操作！");
                        } else {
                            // 否则就移除那个用户
                            ps.println("REMOVE" + "#" + Peruser + "#" + chooseRoomName);
                        }
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(frame, "抱歉，您没有此权限！");
                    }
                } else {
                    // 未点击好友列表
                    javax.swing.JOptionPane.showMessageDialog(frame, "请选择一个好友！");
                }
            }

        }

        // 监听"群聊"发送信息
        public class SendActionListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(nickName + "客户端发送群聊信息");
                ps.println("MSG#" + nickName + "#" + chooseRoomName + "#" + jtf.getText());
                // 发送完后，是输入框中内容为空
                jtf.setText("");
            }
        }

        // 监听群聊"图片"发送
        public class SendImageListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int result = chooser.showOpenDialog(frame);  // 相对于frame居中表示
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    try {
                        // 将文件内容读取到字节数组中
                        byte[] imageBytes = Files.readAllBytes(file.toPath());
                        String imageStr = Base64.encode(imageBytes);
                        String str = "IMG#" + nickName + "#" + chooseRoomName + "#";
                        ps.println(str + imageStr);
                        System.out.println("服务器发送图片数据："+str + imageStr);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }

        // 监听私聊"图片"发送
        public class SendPriImageListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int result = chooser.showOpenDialog(frame);  // 相对于frame居中表示
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    try {
                        // 将文件内容读取到字节数组中
                        byte[] imageBytes = Files.readAllBytes(file.toPath());
                        String imageStr = Base64.encode(imageBytes);
                        String str = "PriIMG#" + nickName + "#" + chooseRoomName + "#";
                        ps.println(str + imageStr);
                        System.out.println("服务器发送图片数据："+str + imageStr);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }

        // 监听点击"私聊"按钮信息
        public class PersonActionListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!userList.isSelectionEmpty()) { // 如果选中了私聊好友对象
                    Peruser = userList.getSelectedValuesList().get(0);// 获得被选择的用户
                    if(!(Peruser.equals(nickName + "(管理员)")) && !(Peruser.equals(nickName)) ){
                        PriWindow(Peruser); // 创建私聊窗口
                        ct.openPriWin = true; // 标记此进程已打开了私聊窗口,后续不会再打开
                        sender = nickName;
                        receiver = Peruser;
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(frame, "不能选择与自己私聊！");
                    }
                } else {
                    javax.swing.JOptionPane.showMessageDialog(frame, "请选择一个好友！");
                }
            }
        }

        // 监听私聊窗口"发送消息"
        public class PriSendListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 此处有一个发送者与接收者身份的转换
                String name = sender;
                if (sender.equals(nickName)) {
                    name = receiver;
                }
                ps.println("PMSG" + "#" + nickName + "#" + name + "#" + jtf1.getText());
                System.out.println(nickName + "客户端发送" + "PMSG" + "#" + nickName + "#" + name + "#" + jtf1.getText());
                jtf1.setText("");
            }
        }

    }

    @Override
    public void run() {
        while (true) {
            try {
                String msg = br.readLine();
                String[] strs = msg.split("#");
                if(strs[0].equals("WARN")){  // 用户名重复，下线
                    javax.swing.JOptionPane.showMessageDialog(sq.frame, "用户名已存在！");
                    // 关闭广场窗口，重新开启一个线程--让用户重新输入昵称
                    sq.closeWindow();
                    try {
                        new Client();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (strs[0].equals("LOGIN")) { // 服务器对用户进入聊天室的消息的转发--LOGIN#nickName#chooseRoomName
                    if (ct == null) { // 在第一个非本用户进入聊天室的时候，本ct还未创建，就不接受此消息
                        continue;
                    }
                    if(strs[2].equals(chooseRoomName)){
                        // 此信息是发送给本聊天室
                        if (!strs[1].equals(nickName)) { // 如果是别人上线--群聊显示欢迎对方
                            // null采用文字的默认样式
                            try {
                                ct.doc.insertString(ct.doc.getLength(), "欢迎" + strs[1] + "进入此聊天室！\n", null);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                            ct.jta.repaint();
                            ct.jta.revalidate();
                        }
                    }

                } else if (strs[0].equals("USER")) { // 传送所有好友信息 USER#nickName#chooseRoomName
                    System.out.println(nickName + "客户端收到USER#" + strs[1]);
                    if (ct == null)
                        continue;
                    if(strs[2].equals(chooseRoomName)){
                        // 如果是管理员，但是这个信息还没存入dl
                        if (strs[1].equals(chooseRoom.getPerName()) && !ct.dl.contains(strs[1] + "(管理员)")) { // 如果是管理员，就加字
                            ct.dl.addElement(strs[1] + "(管理员)");
                            ct.userList.repaint();
                        }
                        // 如果不是管理员，并且这个用户还没存入dl中
                        if (!strs[1].equals(chooseRoom.getPerName()) && !ct.dl.contains(strs[1])) { // 传过来的好友信息可能会包含重复的信息
                            ct.dl.addElement(strs[1]);
                            ct.userList.repaint();
                        }
                    }

                } else if (strs[0].equals("EXIT")) { // 自愿下线  EXIT#nickName#chooseRoom
                    if(strs[2].equals(chooseRoomName)){
                        if (strs[1].equals(chooseRoom.getPerName())) { // 管理员下线，聊天室自动解散
                            if (!strs[1].equals(nickName)) // 此用户端不是管理员端，才会发送此消息！
                            {
                                javax.swing.JOptionPane.showMessageDialog(null, "管理员下线，房间自动解散！");
                                // 聊天室窗口对象销毁
                                ct.frame.dispose();
                                // 广场界面窗口重新出现
                                sq.setVisible(true);
                            }
                            // 移除此房间
                            System.out.println(nickName + "已移除聊天室！");
                            String removeRoomName = strs[2];
                            sq.removeChatRoom(removeRoomName);
                            System.out.println("DELETE" + "#" + chooseRoomName);
                            ps.println("DELETE" + "#" + chooseRoomName); // 发送DELETE,让服务端删除所有的用户
                        }
                        if (!strs[1].equals(nickName)) { // 如果不是自己的下线信息
                            if (ct.jta != null) {
                                try {
                                    ct.doc.insertString(ct.doc.getLength(), strs[1] + "已离开聊天室！\n", null);
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                                ct.jta.repaint();
                                ct.jta.revalidate();
                            }
                            ct.dl.removeElement(strs[1]);
                            ct.userList.repaint();
                        } else { // 如果是自己下线的话
                            ct.dl.removeElement(strs[1]);
                            ct.userList.repaint();
                        }
                    }

                } else if (strs[0].equals("REMOVE")) {   // 强制下线  REMOVE#PerName#chooseName
                    if(strs[2].equals(chooseRoomName)){
                        if (strs[1].equals(nickName)) {
                            // 本用户下线
                            javax.swing.JOptionPane.showMessageDialog(null, "您已被管理员请出聊天室！");
                            ct.frame.dispose();
                            sq.setVisible(true);
                            ct.dl.removeElement(strs[1]);
                            ct.userList.repaint();
                        } else {
                            // 其他端口提示一下
                            try {
                                ct.doc.insertString(ct.doc.getLength(), strs[1] + "已被管理员移出聊天室！\n", null);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                            ct.jta.repaint();
                            ct.jta.revalidate();
                            ct.dl.removeElement(strs[1]);
                            ct.userList.repaint();
                        }
                    }

                } else if (strs[0].equals("COMPONENT")) { // 传来组件信息，在每个用户端面板画出聊天室
                    System.out.println("客户端收到聊天室数据，开始绘制面板");
                    Room newRoom = new Room();
                    newRoom.setPerName(strs[1]);
                    newRoom.setName(strs[2]);
                    newRoom.setTag(strs[3]);
                    newRoom.setInfo(strs[4]);
                    // 判断目前聊天室队列中是否已经有这个聊天室
                    if(!sq.equals(newRoom.getName())){
                        System.out.println("服务器无重复的聊天室");
                        // 没有的话再创建新的聊天室
                        if(newRoom.getTag().equals("游戏")){
                            gameList.add(newRoom);
                            sq.drawChatRoom(gameJpl,newRoom.getTag(), newRoom.getName(), newRoom.getInfo());
                        }else if(newRoom.getTag().equals("生活")){
                            liveList.add(newRoom);
                            sq.drawChatRoom(liveJpl,newRoom.getTag(), newRoom.getName(), newRoom.getInfo());
                        }else if(newRoom.getTag().equals("时事")){
                            eventList.add(newRoom);
                            sq.drawChatRoom(eventJpl,newRoom.getTag(), newRoom.getName(), newRoom.getInfo());
                        }else if(newRoom.getTag().equals("学习")){
                            studyList.add(newRoom);
                            sq.drawChatRoom(studyJpl,newRoom.getTag(), newRoom.getName(), newRoom.getInfo());
                        }
                    }
                } else if (strs[0].equals("MSG")) {     // MSG是群聊消息  MSG#nickName#RoomName#
                    if(strs[2].equals(chooseRoomName)){

                        // 如果此信息是发送给此聊天室的话，就显示消息
                        if (!strs[1].equals(nickName)) {
                            // 别的用户发来的群聊消息
                            try {
                                ct.doc.insertString(ct.doc.getLength(), strs[1] +"："+strs[3]+ "\n", null);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                ct.doc.insertString(ct.doc.getLength(), "我："+strs[3] + "\n", null);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } else if (strs[0].equals("SYSMSG")) {  // SYSMSG是系统消息
                    try {
                        ct.doc.insertString(ct.doc.getLength(), "系统消息：" + strs[1] + "\n", null);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                } else if (strs[0].equals("PMSG")&& (strs[1].equals(nickName)||strs[2].equals(nickName))||(strs[1].equals(nickName+"(管理员)")||strs[2].equals(nickName+"(管理员)"))) { // PMSG是私聊消息
                    // PMSG#nickName#name#chooseName#
                    if (strs.length != 4)
                        continue;
                    // 如果自己已经下线，那么将不会接收到私聊信息
                    if(ct!=null && ct.offLine == false)
                        continue;
                    ct.sender = strs[1];
                    ct.receiver = strs[2];
                    String name = strs[1] == nickName ? strs[2] : strs[1];
                    if (!name.equals(nickName)) // 自己端口发送过去的消息不用提示
                        try {
                            ct.doc.insertString(ct.doc.getLength(), "系统提示：" + name +"给您发来了一条私信\n", null);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    // 接收方第一次收到私聊信息，先要自动弹出私聊窗口
                    if (!strs[1].equals(nickName)) {
                        System.out.println(ct.openPriWin);
                        if (ct.openPriWin == false) {
                            ct.PriWindow(strs[1]);
                            ct.openPriWin = true;
                        }
                        try {
                            ct.priDoc.insertString(ct.priDoc.getLength(), strs[1] + "：" + strs[3] + "\n", null);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            ct.priDoc.insertString(ct.priDoc.getLength(), "我：" + strs[3] + "\n", null);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    }
                }else if (strs[0].equals("IMG")) {  // IMG#nickName#chooseRoomName#imgStr
                    // 群聊图片发送
                    String receivedName = strs[1];
                    if(strs[2].equals(chooseRoomName)){
                        // 如果是发给本聊天室的
                        if(receivedName.equals(nickName)){
                            // 如果是自己发的
                            ct.doc.insertString(ct.doc.getLength(), "我：" , null);
                        }else{
                            ct.doc.insertString(ct.doc.getLength(), strs[1] + "：" , null);
                        }
                        String imgStr = strs[3];
                        System.out.println("接收到的图片发送用户名: " + receivedName);
                        System.out.println("接收到的图片发送数据: " + imgStr);
                        // 处理图片数据
                        byte[] imgBytes = Base64.decode(imgStr.getBytes());
                        ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes); // 将字节数组包装成输入流
                        BufferedImage image = ImageIO.read(bais);
                        ImageIcon icon = new ImageIcon(image); // 转换回图片
                        Style style = ct.doc.addStyle("image", null);
                        StyleConstants.setIcon(style, icon); // 将icon设置为Style对象的属性
                        ct.doc.insertString(ct.doc.getLength(), "\n", null); // 插入内容
                        ct.doc.insertString(ct.doc.getLength(), " ", style);
                        ct.doc.insertString(ct.doc.getLength(), "\n", null);
                    }

                }else if(strs[0].equals("PriIMG")){  // PriIMG#nickName#chooseRoomName#
                    // 私聊图片发送
                    String receivedName = strs[1];
                    if(strs[2].equals(chooseRoomName)){
                        if(receivedName.equals(nickName)){
                            // 如果是自己发的
                            ct.priDoc.insertString(ct.priDoc.getLength(), "我：" , null);
                        }else{
                            ct.priDoc.insertString(ct.priDoc.getLength(), strs[1] + "：" , null);
                        }
                        String imgStr = strs[3];
                        System.out.println("接收到的图片发送用户名: " + receivedName);
                        System.out.println("接收到的图片发送数据: " + imgStr);
                        // 处理图片数据
                        byte[] imgBytes = Base64.decode(imgStr.getBytes());
                        ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes);
                        BufferedImage image = ImageIO.read(bais);
                        ImageIcon icon = new ImageIcon(image);
                        Style style = ct.priDoc.addStyle("image", null);
                        StyleConstants.setIcon(style, icon);
                        ct.priDoc.insertString(ct.priDoc.getLength(), "\n", null);
                        ct.priDoc.insertString(ct.priDoc.getLength(), " ", style);
                        ct.priDoc.insertString(ct.priDoc.getLength(), "\n", null);
                    }
                }

            } catch (IOException | BadLocationException e) {
                javax.swing.JOptionPane.showMessageDialog(null, "系统异常，您已被请出！");
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Client();
    }

}

