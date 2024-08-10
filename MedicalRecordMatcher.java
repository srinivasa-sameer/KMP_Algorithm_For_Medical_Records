import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MedicalRecordMatcher extends JFrame {
  private JTextField keywordField;
  private JTextPane resultPane;
  private File selectedFile;

  public MedicalRecordMatcher() {
    setTitle("KMP Algorithm for Medical Records");
    setSize(800, 600);
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

    keywordField = new JTextField(20);
    topPanel.add(keywordField);

    JButton searchButton = new JButton("Search");
    searchButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleHighlight();
      }
    });
    topPanel.add(searchButton);

    panel.add(topPanel, BorderLayout.NORTH);

    resultPane = new JTextPane();
    resultPane.setContentType("text/html");
    resultPane.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(resultPane);
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

  private void handleHighlight() {
    if (selectedFile == null) {
      JOptionPane.showMessageDialog(this, "Please upload a PDF file first.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    String keyword = keywordField.getText().trim();
    if (keyword.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please enter a keyword to highlight.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    String content = processPDF(selectedFile, keyword);
    displayContent(content, keyword);
  }

  private String processPDF(File file, String keyword) {
    StringBuilder text = new StringBuilder();
    try {
      PDDocument document = Loader.loadPDF(file);
      PDFTextStripper pdfStripper = new PDFTextStripper();
      text.append(pdfStripper.getText(document));
      document.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "Failed to read PDF file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    return text.toString();
  }

  private void displayContent(String content, String keyword) {
    String keywordLower = keyword.toLowerCase();
    String highlightedContent = content.replaceAll("(?i)(" + keyword + ")", "<span style='background-color: yellow;'>$1</span>");
    String htmlContent = "<html><body style='font-family:sans-serif;'>" + highlightedContent.replace("\n", "<br>") + "</body></html>";
    resultPane.setText(htmlContent);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      MedicalRecordMatcher highlighter = new MedicalRecordMatcher();
      highlighter.setVisible(true);
    });
  }
}
