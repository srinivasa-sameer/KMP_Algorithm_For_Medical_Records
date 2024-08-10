import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordMatcher extends JFrame {
  private JTextField keywordField;
  private JTextPane resultArea;
  private File selectedFile;

  public MedicalRecordMatcher() {
    setTitle("PDF Keyword Matcher");
    setSize(600, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    initializeUI();
  }

  private void initializeUI() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());

    JPanel topPanel = new JPanel(new FlowLayout());

    JButton uploadButton = new JButton("Upload PDF");
    uploadButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleFileUpload();
      }
    });
    topPanel.add(uploadButton);

    JLabel keywordLabel = new JLabel("Enter Keyword:");
    topPanel.add(keywordLabel);
    keywordField = new JTextField(20);
    topPanel.add(keywordField);

    JButton searchButton = new JButton("Search");
    searchButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleSearch();
      }
    });
    topPanel.add(searchButton);

    panel.add(topPanel, BorderLayout.NORTH);

    resultArea = new JTextPane();
    resultArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(resultArea);
    panel.add(scrollPane, BorderLayout.CENTER);

    add(panel);
  }

  private void handleFileUpload() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
    int returnValue = fileChooser.showOpenDialog(this);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      selectedFile = fileChooser.getSelectedFile();
      JOptionPane.showMessageDialog(this, "File selected: " + selectedFile.getName());
    }
  }

  private void handleSearch() {
    if (selectedFile == null) {
      JOptionPane.showMessageDialog(this, "Please upload a PDF file first.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    String keyword = keywordField.getText().trim();
    if (keyword.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please enter a keyword to search.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    List<String> matches = processPDF(selectedFile, keyword);
    displayResults(matches, keyword);
  }

  private List<String> processPDF(File file, String keyword) {
    List<String> matches = new ArrayList<>();
    try {
      PDDocument document = Loader.loadPDF(file);
      PDFTextStripper pdfStripper = new PDFTextStripper();
      String text = pdfStripper.getText(document);
      document.close();

      String[] records = text.split("\n\n");  // Assuming records are separated by two new lines
      matches = matchRecords(records, keyword);

    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "Failed to read PDF file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    return matches;
  }

  private List<String> matchRecords(String[] records, String keyword) {
    List<String> matches = new ArrayList<>();
    for (String record : records) {
      if (findOccurrences(record.toLowerCase(), keyword.toLowerCase()) > 0) {
        matches.add(record);
      }
    }
    return matches;
  }

  private int findOccurrences(String text, String pattern) {
    int n = text.length();
    int m = pattern.length();
    int[] pi = computePrefixFunction(pattern);
    int q = 0;
    int occurrences = 0;
    for (int i = 0; i < n; i++) {
      while (q > 0 && pattern.charAt(q) != text.charAt(i)) {
        q = pi[q - 1];
      }
      if (pattern.charAt(q) == text.charAt(i)) {
        q++;
      }
      if (q == m) {
        occurrences++;
        q = pi[q - 1];
      }
    }
    return occurrences;
  }

  private int[] computePrefixFunction(String pattern) {
    int m = pattern.length();
    int[] pi = new int[m];
    int k = 0;
    for (int q = 1; q < m; q++) {
      while (k > 0 && pattern.charAt(k) != pattern.charAt(q)) {
        k = pi[k - 1];
      }
      if (pattern.charAt(k) == pattern.charAt(q)) {
        k++;
      }
      pi[q] = k;
    }
    return pi;
  }

  private void displayResults(List<String> matches, String keyword) {
    resultArea.setText("");  // Clear previous results
    if (matches.isEmpty()) {
      resultArea.setText("No matches found.(Note: Please enter correct spelling)\n");
    } else {
      StyledDocument doc = resultArea.getStyledDocument();
      SimpleAttributeSet normalStyle = new SimpleAttributeSet();
      SimpleAttributeSet keywordStyle = new SimpleAttributeSet();
      StyleConstants.setBackground(keywordStyle, Color.YELLOW);
      StyleConstants.setBold(keywordStyle, true);

      resultArea.setText("Patients with the keyword:\n");
      for (String match : matches) {
        int start = 0;
        String lowerMatch = match.toLowerCase();
        while ((start = lowerMatch.indexOf(keyword.toLowerCase(), start)) != -1) {
          int end = start + keyword.length();
          try {
            doc.insertString(doc.getLength(), match.substring(0, start), normalStyle);
            doc.insertString(doc.getLength(), match.substring(start, end), keywordStyle);
            match = match.substring(end);
            lowerMatch = match.toLowerCase();
            start = 0;
          } catch (BadLocationException e) {
            e.printStackTrace();
          }
        }
        try {
          doc.insertString(doc.getLength(), match, normalStyle);
          doc.insertString(doc.getLength(), "\n\n", normalStyle);
        } catch (BadLocationException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      MedicalRecordMatcher matcher = new MedicalRecordMatcher();
      matcher.setVisible(true);
    });
  }
}
