import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ini4j.Ini;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO
// -Get Icon to build in JAR
// -Option to use old method of checking
// -Pull ini from the device
// -Add check for excel file
// -Output to excel

public class Checker {
    private static File selectedFolder;
    private static File selectedExcel;
    private static final ArrayList<String> problems = new ArrayList<>();
    private static long totalFiles = 0;

    public static void main(String[] args) {
        window();
    }

    public static void window() {
        //Create Window
        JFrame frame = new JFrame("PDU Checker V3.1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 200);
        JPanel panel = new JPanel();
        panel.setLayout(null);

        ImageIcon img = new ImageIcon("src/main/java/icon.png");
        frame.setIconImage(img.getImage());

        //Create objects in window
        //Choose Folder Button
        JButton selectFolder = new JButton("Choose Folder");
        selectFolder.setBounds(15, 15, 200, 50);
        //Chosen folder label
        JButton selectExcel = new JButton("Select Excel File");
        selectExcel.setBounds(250, 15, 200, 50);
        //Start check button
        JButton startCheck = new JButton("Start Check");
        startCheck.setBounds(15, 75, 200, 50);

        //Button Actions
        //Select folder button action
        selectFolder.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                selectedFolder = fileChooser.getSelectedFile();
            }
        });

        //Select Excel File
        selectExcel.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                selectedExcel = fileChooser.getSelectedFile();
            }
        });

        //Start check button action
        startCheck.addActionListener(e -> {
            if (selectedFolder == null) {
                JOptionPane.showMessageDialog(frame, "Please select a folder", "Folder Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedExcel == null) {
                JOptionPane.showMessageDialog(frame, "Please Select a Excel File", "Folder Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //Clear problems in case it is not the first time this is run
            problems.clear();

            //Run the Check
            try {
                runChecker(selectedFolder, selectedExcel);
            } catch (IOException | InvalidFormatException ex) {
                ex.printStackTrace();
            }

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

        //Add objects to panel
        panel.add(selectFolder);
        panel.add(selectExcel);
        panel.add(startCheck);

        //Make frame appear
        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    /**
     * @param selectedFolder The folder containing ini files
     * @param selectedExcel  The configuration excel file
     */
    public static void runChecker(File selectedFolder, File selectedExcel) throws IOException, InvalidFormatException {
        XSSFWorkbook workbook = new XSSFWorkbook(selectedExcel);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Map<String, List<String>> excelDoc = new HashMap<>();
        for (Row row : sheet) { // For each Row.
            if (row.getRowNum() > 5 && row.getCell(1) != null && !row.getCell(1).getStringCellValue().equals("")) {
                List<String> testies = new ArrayList<>();
                testies.add(row.getCell(2).getStringCellValue());
                testies.add(row.getCell(3).getStringCellValue());
                testies.add(row.getCell(4).getStringCellValue());
                testies.add(row.getCell(7).getStringCellValue());
                testies.add(row.getCell(9).getStringCellValue());
                testies.add(row.getCell(10).getStringCellValue());
                testies.add(row.getCell(11).getStringCellValue());
                testies.add(row.getCell(12).getStringCellValue());
                testies.add(row.getCell(13).getStringCellValue());
                testies.add(row.getCell(14).getStringCellValue());
                testies.add(row.getCell(15).getStringCellValue());
                testies.add(row.getCell(16).getStringCellValue());
                excelDoc.put(row.getCell(1).getStringCellValue(), testies);
            }
        }
        workbook.close();

        File[] listOfFiles = selectedFolder.listFiles();
        totalFiles = 0;

        assert listOfFiles != null;
        for (File file : listOfFiles) {
            //Only checks .ini files
            if (!file.toString().endsWith(".ini")) {
                continue;
            }

            Ini ini = new Ini(file);
            String hostname = ini.get("NetworkTCP/IP", "HostName");


            if (ini.get("NetworkWeb", "HTTPS").equals("disabled")) {
                problems.add(hostname + ": HTTPS Disabled");
            }
            if (ini.get("NetworkWeb", "HTTP").equals("enabled")) {
                problems.add(hostname + ": HTTP Enabled");
            }
            if (ini.get("NetworkTelnet", "SSH").equals("disabled")) {
                problems.add(hostname + ": SSH Disabled");
            }
            if (ini.get("NetworkTelnet", "Telnet").equals("enabled")) {
                problems.add(hostname + ": Telnet Enabled");
            }
            if (ini.get("NetworkFTPServer", "Access").equals("disabled")) {
                problems.add(hostname + ": FTP Disabled");
            }
            if (!ini.get("SystemDate/Time", "NTPTimeZone").equals("00:00")) {
                problems.add(hostname + ": Time Zone Incorrect");
            }
            if (!ini.get("SystemDate/Time", "NTPPrimaryServer").equals("192.168.128.1")) {
                problems.add(hostname + ": NTP Primary Server Incorrect ");
            }
            if (!ini.get("SystemDate/Time", "NTPSecondaryServer").equals("192.168.16.1")) {
                problems.add(hostname + ": NTP Secondary Server Incorrect ");
            }
            if (!ini.get("SystemDate/Time", "NTPEnable").equals("enabled")) {
                problems.add(hostname + ": NTP Disabled ");
            }
            if (!ini.get("NetworkTCP/IP", "IPv4").equals("enabled")) {
                problems.add(hostname + ": TCP (IPV4) Disabled ");
            }
            if (!ini.get("NetworkTCP/IP", "DomainName").equals("pg.com")) {
                problems.add(hostname + ": Domain Name Incorrect ");
            }
            if (!ini.get("NetworkTCP/IP", "IPv6").equals("disabled")) {
                problems.add(hostname + ": IPv6 Enabled");
            }
            if (!ini.get("SystemID", "Contact").equals("Netops")) {
                problems.add(hostname + ": SystemID Contact Incorrect");
            }
            if (!ini.get("SystemID", "Message").equals("This is a Procter and Gamble managed device, changes to the device configuration should be authorized through the processes outlined by the ITS Network Services team. Please contact PG NOC at itservices.pg.com if you are unclear of how to proceed.")) {
                problems.add(hostname + ": MOTD Incorrect");
            }
            if (!ini.get("NetworkSNMP", "Access").equals("disabled")) {
                problems.add(hostname + ": SNMP V1 Enabled");
            }
            if (!ini.get("NetworkSNMP", "AccessSnmpIII").equals("enabled")) {
                problems.add(hostname + ": SNMP V3 Disabled");
            }
            if (!ini.get("NetworkSNMP", "UserProfile1Name").equals("PG_RO_PDU_USER")) {
                problems.add(hostname + ": SNMP User Incorrect");
            }
            if (!ini.get("NetworkSNMP", "AccessControlSnmpIII1Enabled").equals("enabled")) {
                problems.add(hostname + ": SNMP User Access Disabled");
            }
            if (!ini.get("NetworkSNMP", "TrapReceiver1NMS").equals("137.182.94.81")) {
                problems.add(hostname + ": SNMP Trap Receiver is Incorrect");
            }
            if (!ini.get("NetworkSNMP", "TrapReceiver1Enabled").equals("enabled")) {
                problems.add(hostname + ": SNMP Trap Receiver Disabled");
            }
            if (!ini.get("NetworkSNMP", "TrapReceiver1AuthTrap").equals("disabled")) {
                problems.add(hostname + ": SNMP Trap Receiver Auth Trap Enabled");
            }
            if (!ini.get("NetworkSNMP", "TrapReceiver1TrapType").equals("SNMPv3")) {
                problems.add(hostname + ": SNMP Trap Receiver Incorrect Version");
            }
            if (!ini.get("NetworkTCP/IP", "SystemIP").equals(excelDoc.get(hostname).get(0))) {
                problems.add(hostname + ": IP Address Incorrect");
            }
            if (!ini.get("NetworkTCP/IP", "SubnetMask").equals(excelDoc.get(hostname).get(1))) {
                problems.add(hostname + ": Subnet Mask Incorrect");
            }
            if (!ini.get("NetworkTCP/IP", "DefaultGateway").equals(excelDoc.get(hostname).get(2))) {
                problems.add(hostname + ": Default Gateway Incorrect");
            }
            if (!('"' + ini.get("SystemID", "Location") + '"').equals(excelDoc.get(hostname).get(3))) {
                problems.add(hostname + ": System Location Incorrect");
            }
            if (!ini.get("Outlet", "OUTLET_NAME_A1").equals(excelDoc.get(hostname).get(4))) {
                problems.add(hostname + ": Outlet 1 Name Incorrect");
            }
            if (!ini.get("Outlet", "OUTLET_NAME_A2").equals(excelDoc.get(hostname).get(5))) {
                problems.add(hostname + ": Outlet 2 Name Incorrect");
            }
            if (!ini.get("Outlet", "OUTLET_NAME_A3").equals(excelDoc.get(hostname).get(6))) {
                problems.add(hostname + ": Outlet 3 Name Incorrect");
            }
            if (!ini.get("Outlet", "OUTLET_NAME_A4").equals(excelDoc.get(hostname).get(7))) {
                problems.add(hostname + ": Outlet 4 Name Incorrect");
            }
            if (!ini.get("Outlet", "OUTLET_NAME_A5").equals(excelDoc.get(hostname).get(8))) {
                problems.add(hostname + ": Outlet 5 Name Incorrect");
            }
            if (!ini.get("Outlet", "OUTLET_NAME_A6").equals(excelDoc.get(hostname).get(9))) {
                problems.add(hostname + ": Outlet 6 Name Incorrect");
            }
            if (!ini.get("Outlet", "OUTLET_NAME_A7").equals(excelDoc.get(hostname).get(10))) {
                problems.add(hostname + ": Outlet 7 Name Incorrect");
            }
            if (!ini.get("Outlet", "OUTLET_NAME_A8").equals(excelDoc.get(hostname).get(11))) {
                problems.add(hostname + ": Outlet 8 Name Incorrect");
            }
            if (!delayTranslate(excelDoc.get(hostname).get(4)).equals(ini.get("Outlet", "OUTLET_POWER_ON_DELAY_A1"))) {
                problems.add(hostname + ": Outlet 1 Delay Incorrect");
            }
            if (!delayTranslate(excelDoc.get(hostname).get(5)).equals(ini.get("Outlet", "OUTLET_POWER_ON_DELAY_A2"))) {
                problems.add(hostname + ": Outlet 2 Delay Incorrect");
            }
            if (!delayTranslate(excelDoc.get(hostname).get(6)).equals(ini.get("Outlet", "OUTLET_POWER_ON_DELAY_A3"))) {
                problems.add(hostname + ": Outlet 3 Delay Incorrect");
            }
            if (!delayTranslate(excelDoc.get(hostname).get(7)).equals(ini.get("Outlet", "OUTLET_POWER_ON_DELAY_A4"))) {
                problems.add(hostname + ": Outlet 4 Delay Incorrect");
            }
            if (!delayTranslate(excelDoc.get(hostname).get(8)).equals(ini.get("Outlet", "OUTLET_POWER_ON_DELAY_A5"))) {
                problems.add(hostname + ": Outlet 5 Delay Incorrect");
            }
            if (!delayTranslate(excelDoc.get(hostname).get(9)).equals(ini.get("Outlet", "OUTLET_POWER_ON_DELAY_A6"))) {
                problems.add(hostname + ": Outlet 6 Delay Incorrect");
            }
            if (!delayTranslate(excelDoc.get(hostname).get(10)).equals(ini.get("Outlet", "OUTLET_POWER_ON_DELAY_A7"))) {
                problems.add(hostname + ": Outlet 7 Delay Incorrect");
            }
            if (!delayTranslate(excelDoc.get(hostname).get(11)).equals(ini.get("Outlet", "OUTLET_POWER_ON_DELAY_A8"))) {
                problems.add(hostname + ": Outlet 8 Delay Incorrect");
            }

            totalFiles++;
        }
    }

    /**
     * @param olname The name of the outlet in the Excel document
     * @return returns what the olDelay config should be
     */
    public static String delayTranslate(String olname) {
        if (olname.equalsIgnoreCase("shutdown") || olname.equalsIgnoreCase("unused") || olname.equalsIgnoreCase("")) {
            return "-1";
        } else {
            return "0";
        }
    }
}
