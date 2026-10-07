import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class LoginSystem implements ActionListener {

    // Design System Palette
    private static final Color COLOR_CYAN        = new Color(0, 180, 216);
    private static final Color COLOR_CYAN_HOVER  = new Color(72, 202, 228);
    private static final Color COLOR_TEXT_MAIN   = new Color(240, 242, 245);
    private static final Color COLOR_TEXT_MUTED  = new Color(160, 167, 175);
    private static final Color COLOR_FIELD_BG    = new Color(30, 33, 40, 180);
    private static final Color COLOR_BORDER      = new Color(70, 75, 85);
    private static final Color COLOR_BORDER_FOCUS = new Color(0, 180, 216);

    private JFrame frame;
    private AnimatedBackgroundPanel mainBackground;
    private GlassCardPanel cardPanel;
    private JPanel slidingContainer;

    // Login Form Components
    private AnimatedTextField loginUsername;
    private AnimatedPasswordField loginPassword;
    private JButton loginBtn;
    private JButton switchToRegisterBtn;

    // Register Form Components
    private AnimatedTextField regUsername;
    private AnimatedPasswordField regPassword;
    private JProgressBar strengthMeter;
    private JButton registerBtn;
    private JButton switchToLoginBtn;

    private final LocalStore store;

    public LoginSystem() {
        store=new LocalStore();
        initUI();
    }

    private void initUI() {
        frame = new JFrame("Expense Tracker - Premium Portal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(880, 540);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // Animated Background Host
        mainBackground = new AnimatedBackgroundPanel();
        mainBackground.setLayout(new GridBagLayout());

        // Central Glass Panel
        cardPanel = new GlassCardPanel(24);
        cardPanel.setPreferredSize(new Dimension(800, 460));
        cardPanel.setLayout(new GridLayout(1, 2));

        // Build Left Hero & Right Slider
        cardPanel.add(buildHeroSection());
        cardPanel.add(buildSlidingFormSection());

        mainBackground.add(cardPanel);
        frame.setContentPane(mainBackground);
        frame.setVisible(true);
    }

    // Hero Section (Left Column)
    private JPanel buildHeroSection() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(40, 35, 40, 35));

        JLabel logo = new JLabel("<html><span style='font-size:32px;'>\uD83D\uDCB3</span><br><b style='color:#00B4D8; font-size:24px;'>Expense Tracker</b></html>");
        logo.setForeground(COLOR_TEXT_MAIN);

        JLabel subtitle = new JLabel("<html><p style='width:220px; color:#A0A7B0; font-size:12px; line-height:1.4;'>Smart budgeting, expense analytics, and real-time personal finance control.</p></html>");

        String[] tips = {
            "\uD83D\uDCA1 <b>Tip:</b> Stick to the 50/30/20 rule to maintain healthy savings.",
            "\uD83D\uDCA1 <b>Tip:</b> Audit monthly subscriptions to cut unused costs.",
            "\uD83D\uDCA1 <b>Tip:</b> Log micro-expenses; daily coffee counts end up big!"
        };
        JLabel tipLabel = new JLabel("<html>" + tips[(int) (Math.random() * tips.length)] + "</html>");
        tipLabel.setForeground(COLOR_TEXT_MAIN);
        tipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel topContainer = new JPanel(new GridLayout(2, 1, 0, 10));
        topContainer.setOpaque(false);
        topContainer.add(logo);
        topContainer.add(subtitle);

        hero.add(topContainer, BorderLayout.NORTH);
        hero.add(tipLabel, BorderLayout.SOUTH);

        return hero;
    }

    // Sliding Container for Smooth View Transitions
    private JPanel buildSlidingFormSection() {
        JPanel outerClip = new JPanel(null);
        outerClip.setOpaque(false);

        slidingContainer = new JPanel(null);
        slidingContainer.setOpaque(false);
        slidingContainer.setBounds(0, 0, 800, 460);

        JPanel loginForm = buildLoginForm();
        loginForm.setBounds(0, 0, 400, 460);

        JPanel regForm = buildRegisterForm();
        regForm.setBounds(400, 0, 400, 460);

        slidingContainer.add(loginForm);
        slidingContainer.add(regForm);

        outerClip.add(slidingContainer);
        return outerClip;
    }

    // Login Form Panel
    private JPanel buildLoginForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(40, 30, 40, 35));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(COLOR_TEXT_MAIN);

        JLabel subTitle = new JLabel("Sign in to your financial account");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(COLOR_TEXT_MUTED);

        loginUsername = new AnimatedTextField("Username");
        loginPassword = new AnimatedPasswordField("Password");

        JCheckBox showPassword = new JCheckBox("Show Password");
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        showPassword.setForeground(COLOR_TEXT_MUTED);
        showPassword.setOpaque(false);
        showPassword.setFocusPainted(false);
        showPassword.addActionListener(e -> loginPassword.setEchoChar(showPassword.isSelected() ? (char) 0 : '•'));

        loginBtn = createButton("Sign In", COLOR_CYAN, COLOR_CYAN_HOVER, Color.WHITE);
        loginBtn.addActionListener(this);

        switchToRegisterBtn = createButton("Need an account? Register", new Color(0, 0, 0, 0), new Color(255, 255, 255, 20), COLOR_TEXT_MUTED);
        switchToRegisterBtn.addActionListener(e -> slideToPanel(1));

        form.add(title);
        form.add(subTitle);
        form.add(Box.createVerticalStrut(25));
        form.add(loginUsername);
        form.add(Box.createVerticalStrut(15));
        form.add(loginPassword);
        form.add(Box.createVerticalStrut(8));
        form.add(showPassword);
        form.add(Box.createVerticalStrut(20));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(10));
        form.add(switchToRegisterBtn);

        return form;
    }

    // Register Form Panel
    private JPanel buildRegisterForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(30, 30, 30, 35));

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(COLOR_TEXT_MAIN);

        JLabel subTitle = new JLabel("Join Expense Tracker in seconds");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(COLOR_TEXT_MUTED);

        regUsername = new AnimatedTextField("Username");
        regPassword = new AnimatedPasswordField("Password");

        strengthMeter = new JProgressBar(0, 100);
        strengthMeter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        strengthMeter.setBorderPainted(false);
        strengthMeter.setBackground(new Color(40, 44, 52));
        strengthMeter.setForeground(Color.GRAY);
        strengthMeter.setAlignmentX(Component.LEFT_ALIGNMENT);

        regPassword.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { eval(); }
            public void removeUpdate(DocumentEvent e) { eval(); }
            public void changedUpdate(DocumentEvent e) { eval(); }

            private void eval() {
                String pwd = new String(regPassword.getPassword());
                int score = calculatePasswordStrength(pwd);
                strengthMeter.setValue(score);
                if (pwd.isEmpty()) {
                    strengthMeter.setForeground(new Color(40, 44, 52));
                } else if (score < 40) {
                    strengthMeter.setForeground(new Color(239, 68, 68));
                } else if (score < 75) {
                    strengthMeter.setForeground(new Color(245, 158, 11));
                } else {
                    strengthMeter.setForeground(new Color(34, 197, 94));
                }
            }
        });

        registerBtn = createButton("Register Now", COLOR_CYAN, COLOR_CYAN_HOVER, Color.WHITE);
        registerBtn.addActionListener(this);

        switchToLoginBtn = createButton("Already registered? Sign In", new Color(0, 0, 0, 0), new Color(255, 255, 255, 20), COLOR_TEXT_MUTED);
        switchToLoginBtn.addActionListener(e -> slideToPanel(0));

        form.add(title);
        form.add(subTitle);
        form.add(Box.createVerticalStrut(20));
        form.add(regUsername);
        form.add(Box.createVerticalStrut(12));
        form.add(regPassword);
        form.add(Box.createVerticalStrut(6));
        form.add(strengthMeter);
        form.add(Box.createVerticalStrut(18));
        form.add(registerBtn);
        form.add(Box.createVerticalStrut(10));
        form.add(switchToLoginBtn);

        return form;
    }

    // Slide Transition (0 = Login, 1 = Register)
    private void slideToPanel(int targetIndex) {
        int targetX = targetIndex == 0 ? 0 : -400;
        Timer timer = new Timer(10, null);
        timer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int currentX = slidingContainer.getX();
                if (currentX == targetX) {
                    timer.stop();
                } else {
                    int step = (targetX - currentX) / 4;
                    if (Math.abs(step) < 1) step = (targetX > currentX) ? 1 : -1;
                    slidingContainer.setLocation(currentX + step, 0);
                }
            }
        });
        timer.start();
    }

    // Toast Notification Manager
    private void showToast(String message, boolean isError) {
        JWindow toast = new JWindow(frame);
        JPanel content = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isError ? new Color(220, 38, 38, 230) : new Color(22, 163, 74, 230));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        content.setOpaque(false);

        JLabel lbl = new JLabel(message);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        content.add(lbl);

        toast.setContentPane(content);
        toast.pack();

        Point p = frame.getLocationOnScreen();
        toast.setLocation(p.x + (frame.getWidth() - toast.getWidth()) / 2, p.y + frame.getHeight() - 70);
        toast.setVisible(true);

        Timer timer = new Timer(2500, e -> {
            toast.setVisible(false);
            toast.dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }

    // Non-Blocking Asynchronous DB Connection
    void connectDatabaseAsync() { /* No-op retained for UI compatibility. */ }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) loginUser();
        if (e.getSource() == registerBtn) registerUser();
    }

    // Asynchronous User Registration
void registerUser(){
    String user=regUsername.getText().trim(),pass=new String(regPassword.getPassword());if(user.isEmpty()||pass.isEmpty()){triggerShakeAnimation();showToast("Please complete all details.",true);return;}
    new SwingWorker<Boolean,Void>(){String errorMsg;protected Boolean doInBackground(){try{return store.register(user,pass)!=null;}catch(Exception ex){errorMsg=ex.getMessage();return false;}}protected void done(){try{if(get()){showToast("Registration Successful! Please sign in.",false);regPassword.setText("");slideToPanel(0);}else{triggerShakeAnimation();showToast(errorMsg!=null?errorMsg:"Username already exists.",true);}}catch(Exception ex){showToast("Error processing request.",true);}}}.execute();
}


    // Asynchronous User Login
void loginUser(){
    String user=loginUsername.getText().trim(),pass=new String(loginPassword.getPassword());if(user.isEmpty()||pass.isEmpty()){triggerShakeAnimation();showToast("Please enter both username and password.",true);return;}
    new SwingWorker<UserAccount,Void>(){protected UserAccount doInBackground(){return store.login(user,pass);}protected void done(){try{UserAccount a=get();if(a!=null)openDashboard(a.id);else{triggerShakeAnimation();showToast("Invalid Username or Password.",true);loginPassword.setText("");}}catch(Exception ex){showToast("Login processing failed.",true);}}}.execute();
}


    // Password Hashing via Standard Library SHA-256 (Pure Java)
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // Fallback
        }
    }

    // DIRECT DASHBOARD LAUNCH FIX
private void openDashboard(int userId){
    frame.setVisible(false);frame.dispose();
    SwingUtilities.invokeLater(()->new DashboardUI2(store,userId).setVisible(true));
}


    private void triggerShakeAnimation() {
        Point original = cardPanel.getLocation();
        Timer timer = new Timer(25, null);
        final int[] count = {0};
        final int[] offsets = {-10, 10, -8, 8, -5, 5, -2, 2, 0};

        timer.addActionListener(e -> {
            if (count[0] < offsets.length) {
                cardPanel.setLocation(original.x + offsets[count[0]], original.y);
                count[0]++;
            } else {
                cardPanel.setLocation(original);
                timer.stop();
            }
        });
        timer.start();
    }

    private int calculatePasswordStrength(String pwd) {
        int score = 0;
        if (pwd.length() >= 8) score += 30;
        if (pwd.matches(".*[A-Z].*")) score += 20;
        if (pwd.matches(".*[0-9].*")) score += 25;
        if (pwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score += 25;
        return score;
    }

    private JButton createButton(String text, Color baseBg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBackground(baseBg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hoverBg); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(baseBg); }
        });
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginSystem::new);
    }

    // Custom Components
    static class AnimatedBackgroundPanel extends JPanel {
        private float angle = 0;

        public AnimatedBackgroundPanel() {
            Timer timer = new Timer(40, e -> {
                angle += 0.02f;
                if (angle > Math.PI * 2) angle = 0;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();

            int x1 = (int) (w / 2 + Math.cos(angle) * w / 2);
            int y1 = (int) (h / 2 + Math.sin(angle) * h / 2);
            int x2 = (int) (w / 2 - Math.cos(angle) * w / 2);
            int y2 = (int) (h / 2 - Math.sin(angle) * h / 2);

            Color c1 = new Color(15, 23, 42);
            Color c2 = new Color(30, 27, 75);

            GradientPaint gp = new GradientPaint(x1, y1, c1, x2, y2, c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    static class GlassCardPanel extends JPanel {
        private final int cornerRadius;

        public GlassCardPanel(int radius) {
            this.cornerRadius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(25, 30, 40, 210));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

            g2.setColor(new Color(255, 255, 255, 30));
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));

            g2.dispose();
        }
    }

    static class AnimatedTextField extends JTextField {
        private final String placeholder;
        private boolean isFocused = false;

        public AnimatedTextField(String placeholder) {
            this.placeholder = placeholder;
            setBackground(COLOR_FIELD_BG);
            setForeground(COLOR_TEXT_MAIN);
            setCaretColor(COLOR_TEXT_MAIN);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            updateBorder();

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { isFocused = true; updateBorder(); repaint(); }
                @Override public void focusLost(FocusEvent e) { isFocused = false; updateBorder(); repaint(); }
            });
        }

        private void updateBorder() {
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isFocused ? COLOR_BORDER_FOCUS : COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_TEXT_MUTED);
                g2.setFont(getFont());
                Insets insets = getInsets();
                g2.drawString(placeholder, insets.left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                g2.dispose();
            }
        }
    }

    static class AnimatedPasswordField extends JPasswordField {
        private final String placeholder;
        private boolean isFocused = false;

        public AnimatedPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setEchoChar('•');
            setBackground(COLOR_FIELD_BG);
            setForeground(COLOR_TEXT_MAIN);
            setCaretColor(COLOR_TEXT_MAIN);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            updateBorder();

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { isFocused = true; updateBorder(); repaint(); }
                @Override public void focusLost(FocusEvent e) { isFocused = false; updateBorder(); repaint(); }
            });
        }

        private void updateBorder() {
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isFocused ? COLOR_BORDER_FOCUS : COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0 && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_TEXT_MUTED);
                g2.setFont(getFont());
                Insets insets = getInsets();
                g2.drawString(placeholder, insets.left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                g2.dispose();
            }
        }
    }
}