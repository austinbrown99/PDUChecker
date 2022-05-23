import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Checker {
    private static File selectedFolder;
    private static boolean hostnameEnabled = true;
    private static boolean puttyCheckEnabled = true;
    private static final ArrayList<String> problems = new ArrayList<>();
    private static long totalFiles = 0;

    public static void main(String[] args) {
        window();
    }

    public static void window() {
        //Create Window
        JFrame frame = new JFrame("PDU Checker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 200);
        JPanel panel = new JPanel();
        panel.setLayout(null);


        //Create objects in window
        //Choose Folder Button
        JButton selectFolder = new JButton("Choose Folder");
        selectFolder.setBounds(15, 15, 200, 50);
        //Chosen folder label
        JLabel folderLabel = new JLabel();
        folderLabel.setText("Folder Selected: ");
        folderLabel.setBounds(250, 15, 500, 50);
        //Start check button
        JButton startCheck = new JButton("Start Check");
        startCheck.setBounds(15, 75, 200, 50);
        //Check hostname
        JCheckBox hostnameToggle = new JCheckBox();
        hostnameToggle.setBounds(250, 75, 175, 50);
        hostnameToggle.setSelected(true);
        hostnameToggle.setText("Check for hostnames");
        //Check Putty
        JCheckBox puttyToggle = new JCheckBox();
        puttyToggle.setBounds(425, 75, 200, 50);
        puttyToggle.setSelected(true);
        puttyToggle.setText("Check for Putty");

        //Button Actions
        //Select folder button action
        selectFolder.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                selectedFolder = fileChooser.getSelectedFile();
                folderLabel.setText("Folder Selected: " + selectedFolder);
            }
        });

        //Start check button action
        startCheck.addActionListener(e -> {
            if (selectedFolder == null) {
                JOptionPane.showMessageDialog(frame, "Please select a folder", "Folder Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            //Clear problems in case it is not the first time this is run
            problems.clear();

            //Run the Check
            runChecker(selectedFolder);

            //Results Window
            JFrame resultFrame = new JFrame("PDU Check Result");
            resultFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            resultFrame.setSize(735, 350);
            JPanel resultPanel = new JPanel();
            resultPanel.setLayout(null);
            resultFrame.setContentPane(resultPanel);
            resultFrame.setVisible(false);

            //Create label full of errors and display the window
            JList<String> displayList = new JList<>(problems.toArray(new String[0]));
            JScrollPane scrollPane = new JScrollPane(displayList);
            scrollPane.setBounds(10, 10, 700, 200);

            //Close Button
            JButton resultsClose = new JButton("Close");
            resultsClose.setBounds(10, 220, 150, 75);
            resultsClose.addActionListener(e1 -> {
                resultFrame.getContentPane().remove(scrollPane);
                resultFrame.getContentPane().remove(resultFrame);
                resultFrame.dispose();
            });

            //Total Scanned Label
            JLabel totalLabel = new JLabel();
            totalLabel.setText(totalFiles + " files scanned");
            totalLabel.setBounds(200, 220, 150, 37);
            totalLabel.setVerticalAlignment(SwingConstants.CENTER);
            totalLabel.setHorizontalAlignment(SwingConstants.CENTER);

            //Total Problems
            JLabel totalProblems = new JLabel();
            totalProblems.setText(problems.size() + " oopsies");
            totalProblems.setBounds(200, 257, 150, 37);
            totalProblems.setVerticalAlignment(SwingConstants.CENTER);
            totalProblems.setHorizontalAlignment(SwingConstants.CENTER);

            //Create File Button
            JButton createFile = new JButton("Create \n File");
            createFile.setBounds(400, 220, 150, 75);
            createFile.addActionListener(e1 -> {
                File resultFile = new File(selectedFolder + "\\result.txt");
                try {
                    boolean result = resultFile.createNewFile();
                    FileWriter writer = new FileWriter(resultFile);
                    if (result) { //Do something if the file doesn't exist when creating it
                        for (String str : problems) {
                            writer.write(str + System.lineSeparator());
                        }
                    } else { //Do something if the file already exists when creating it
                        //I know this is the same as above, but I don't care to make it better. It works this way
                        for (String str : problems) {
                            writer.write(str + System.lineSeparator());
                        }
                    }
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            //Copy Button
            StringBuilder problemCopyString = new StringBuilder();
            for (String i : problems) {
                problemCopyString.append("\n").append(i);
            }
            JButton copyButton = new JButton("Copy");
            copyButton.setBounds(560, 220, 150, 75);
            String finalProblemCopyString = problemCopyString.toString();
            copyButton.addActionListener(e1 -> {
                StringSelection stringSelection = new StringSelection(finalProblemCopyString);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            });

            //Add files to the frame
            resultFrame.getContentPane().add(createFile);
            resultFrame.getContentPane().add(copyButton);
            resultFrame.getContentPane().add(totalProblems);
            resultFrame.getContentPane().add(totalLabel);
            resultFrame.getContentPane().add(resultsClose);
            resultFrame.getContentPane().add(scrollPane);
            resultFrame.setVisible(true);
        });

        //Hostname toggle button action
        hostnameToggle.addActionListener(e -> hostnameEnabled = hostnameToggle.isSelected());

        //Putty toggle button action
        puttyToggle.addActionListener(e -> puttyCheckEnabled = puttyToggle.isSelected());

        //Add objects to panel
        panel.add(selectFolder);
        panel.add(folderLabel);
        panel.add(startCheck);
        panel.add(hostnameToggle);
        panel.add(puttyToggle);

        //Make frame appear
        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    public static void runChecker(File selectedFolder) {
        File[] listOfFiles = selectedFolder.listFiles();
        boolean putty = false;

        assert listOfFiles != null;
        for (File file : listOfFiles) {

            if (!file.toString().endsWith(".log")) {
                continue;
            }

            boolean timeZoneFound = false, PrimaryNTP = false, SecondaryNTP = false, DomainName = false, Contact = false,
                    MOTD = false, ReceiverIP = false, Upgrade = false, IOS = false, Hostname = false;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) { // classic way of reading a file line-by-line
                    putty = false;
                    if (line.replaceAll("\\s+", "").contains("PuTTYlog") && puttyCheckEnabled) {
                        problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Putty Detected. Not Scanning");
                        putty = true;
                        break;
                    }
                    if (!putty) {
                        if (line.replaceAll("\\s+", "").contains("Http:enabled")) {
                            problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Http is enabled");
                        }
                        if (line.replaceAll("\\s+", "").contains("Https:disabled")) {
                            problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Https is disabled");
                        }
                        if (line.replaceAll("\\s+", "").contains("SSH:disabled")) {
                            problems.add(file.getName().substring(0, file.getName().length() - 4) + ": SSH is disabled");
                        }
                        if (line.replaceAll("\\s+", "").contains("Telnet:enabled")) {
                            problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Telnet is enabled");
                        }
                        if (line.replaceAll("\\s+", "").contains("Service:disabled")) {
                            problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Telnet is enabled");
                        }
                        if (line.contains("Time Zone: 00:00")) {
                            timeZoneFound = true;
                        }
                        if (line.replaceAll("\\s+", "").contains("ActivePrimaryNTPServer:192.168.128.1")) {
                            PrimaryNTP = true;
                        }
                        if (line.replaceAll("\\s+", "").contains("ActiveSecondaryNTPServer:192.168.16.1")) {
                            SecondaryNTP = true;
                        }
                        if (line.replaceAll("\\s+", "").equalsIgnoreCase("NTPstatus:Disabled")) {
                            problems.add(file.getName().substring(0, file.getName().length() - 4) + ": NTP is disabled");
                        }
                        if (line.replaceAll("\\s+", "").equalsIgnoreCase("IPv4:disabled")) {
                            problems.add(file.getName().substring(0, file.getName().length() - 4) + ": IPV4 Disabled");
                        }
                        if (line.replaceAll("\\s+", "").contains("DomainName:pg.com")) {
                            DomainName = true;
                        }
                        if (line.replaceAll("\\s+", "").contains("HostNameSync:disabled")) {
                            problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Host Name Sync is disabled");
                        }
                        if (line.replaceAll("\\s+", "").contains("Contact:Netops")) {
                            Contact = true;
                        }
                        if (line.replaceAll("\\s+", "").contains("Message:T")) {
                            MOTD = true;
                        }
                        if (line.replaceAll("\\s+", "").contains("SNMPv1:enabled")) {
                            problems.add(file.getName().substring(0, file.getName().length() - 4) + ": SNMPv1 is enabled");
                        }
                        if (line.replaceAll("\\s+", "").contains("SNMPV3:disabled")) {
                            problems.add(file.getName().substring(0, file.getName().length() - 4) + ": SNMPv3 is disabled");
                        }
                        if (line.replaceAll("\\s+", "").contains("ReceiverIP:137.182.94.81")) {
                            ReceiverIP = true;
                        }
                        if (line.replaceAll("\\s+", "").contains("Stat:P+N+A+")) {
                            Upgrade = true;
                        }
                        if (line.replaceAll("\\s+", "").contains("App:rpdu2g:v7.0.6")) {
                            IOS = true;
                        }
                        if (line.replaceAll("\\s+", "").contains("HostName:" + file.getName().substring(0, file.getName().length() - 4)) && hostnameEnabled) {
                            Hostname = true;
                        }
                        if (line.contains("Outlet")) {
                            problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Outlets not named");
                        }
                    }
                }
            } catch (IOException ignored) {
            }
            if (!putty) {
                if (!timeZoneFound) {
                    problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Time zone is incorrect");
                }
                if (!PrimaryNTP) {
                    problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Time zone is incorrect");
                }
                if (!SecondaryNTP) {
                    problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Primary NTP server incorrect");
                }
                if (!DomainName) {
                    problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Primary NTP server incorrect");
                }
                if (!Contact) {
                    problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Contact is incorrect");
                }
                if (!MOTD) {
                    problems.add(file.getName().substring(0, file.getName().length() - 4) + ": MOTD is incorrect");
                }
                if (!ReceiverIP) {
                    problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Receiver is incorrect");
                }
                if (!Upgrade) {
                    problems.add(file.getName().substring(0, file.getName().length() - 4) + ": IOS was upgraded incorrectly");
                }
                if (!IOS) {
                    problems.add(file.getName().substring(0, file.getName().length() - 4) + ": IOS is incorrect");
                }
                if (!Hostname && hostnameEnabled) {
                    problems.add(file.getName().substring(0, file.getName().length() - 4) + ": Hostname is incorrect");
                }
            }
        }
        totalFiles = Arrays.stream(listOfFiles).count();
    }
}
