import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.*;

public class WaterReminderPro {

    private JFrame frame;
    private JTextField intervalField;
    private JButton startButton, stopButton;
    private Timer timer;
    private TrayIcon trayIcon;

    private static final String CONFIG_FILE = "config.txt";

    public WaterReminderPro() {
        createUI();
        setupSystemTray();
        loadSettings();
    }

    private void createUI() {
        frame = new JFrame("💧 Water Reminder Pro");
        frame.setSize(350, 200);
        frame.setLayout(new GridLayout(4, 1, 10, 10));
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JLabel label = new JLabel("Interval (minutes):", JLabel.CENTER);

        intervalField = new JTextField("30");
        intervalField.setHorizontalAlignment(JTextField.CENTER);

        startButton = new JButton("▶ Start");
        stopButton = new JButton("⏸ Stop");

        frame.add(label);
        frame.add(intervalField);
        frame.add(startButton);
        frame.add(stopButton);

        startButton.addActionListener(e -> startReminder());
        stopButton.addActionListener(e -> stopReminder());

        // Minimize to tray
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
            }
        });

        frame.setVisible(true);
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) return;

        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

        PopupMenu popup = new PopupMenu();

        MenuItem openItem = new MenuItem("Open");
        MenuItem exitItem = new MenuItem("Exit");

        openItem.addActionListener(e -> frame.setVisible(true));
        exitItem.addActionListener(e -> System.exit(0));

        popup.add(openItem);
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, "Water Reminder", popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (Exception e) {
            System.out.println("Tray error");
        }
    }

    private void startReminder() {
        try {
            int minutes = Integer.parseInt(intervalField.getText());
            int delay = minutes * 60 * 1000;

            saveSettings(minutes);

            if (timer != null) timer.cancel();

            timer = new Timer();

            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    showNotification();
                }
            }, delay, delay);

            showTrayMessage("Started", "Reminder every " + minutes + " min");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Invalid input!");
        }
    }

    private void stopReminder() {
        if (timer != null) {
            timer.cancel();
            showTrayMessage("Stopped", "Reminder stopped");
        }
    }

    private void showNotification() {
        playSound();
        showTrayMessage("💧 Reminder", "Time to drink water!");
    }

    private void showTrayMessage(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    private void playSound() {
        try {
            File file = new File("alert.wav");
            AudioInputStream audio = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            System.out.println("Sound error");
        }
    }

    private void saveSettings(int minutes) {
        try (FileWriter fw = new FileWriter(CONFIG_FILE)) {
            fw.write(String.valueOf(minutes));
        } catch (Exception ignored) {}
    }

    private void loadSettings() {
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
            intervalField.setText(br.readLine());
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WaterReminderPro::new);
    }
}