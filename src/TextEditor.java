import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class TextEditor extends JFrame {
    JTextArea displayTxt;
    File currentFile; // Track the currently opened file
    Font currentFont; // Track the current font
    private JFrame activeShortcutsWindow;
    private UndoManager undoManager;

    public TextEditor() {
        setTitle("Shaun Text Editor");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        displayTxt = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(displayTxt);

        add(scrollPane, BorderLayout.CENTER);

        createMenuBar();
        
        // Initialize the UndoManager
        undoManager = new UndoManager();
        displayTxt.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        // Register keyboard shortcuts
        registerKeyboardShortcuts();

        setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem openMenuItem = new JMenuItem("<html>Open");
        JMenuItem saveMenuItem = new JMenuItem("<html>Save");
        JMenuItem saveAsMenuItem = new JMenuItem("<html>Save As");
        JMenuItem exitMenuItem = new JMenuItem("<html>Exit");


        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoMenuItem = new JMenuItem("Undo");
        JMenuItem redoMenuItem = new JMenuItem("Redo");
        JMenuItem cutMenuItem = new JMenuItem("Cut");
        JMenuItem copyMenuItem = new JMenuItem("Copy");
        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        JMenuItem selectAllMenuItem = new JMenuItem("Select All");

        JMenu formatMenu = new JMenu("Format");
        JMenuItem zoomInMenuItem = new JMenuItem("Zoom In +");
        JMenuItem zoomOutMenuItem = new JMenuItem("Zoom Out -");
        
        JMenu viewMenu = new JMenu("View");
        JMenuItem themeMenuItem = new JMenuItem("Set to Dark");
        JMenuItem shortcutMenuItem = new JMenuItem("Shortcuts");

        openMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });

        saveAsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveAsFile();
            }
        });

        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        
        undoMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
        
        redoMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });
        
        // Cut: Ctrl+X
        cutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayTxt.cut();
            }
        });

        // Copy: Ctrl+C
        copyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayTxt.copy();
            }
        });
        
        // Copy: Ctrl+V
        pasteMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayTxt.paste();
            }
        });

        // Delete: Del
        deleteMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int start = displayTxt.getSelectionStart();
                int end = displayTxt.getSelectionEnd();
                displayTxt.replaceRange("", start, end);
            }
        });

        // Select All: Ctrl+A
        selectAllMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayTxt.selectAll();
            }
        });
        
        zoomInMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoomIn();
            }
        });

        zoomOutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoomOut();
            }
        });
        
        themeMenuItem.addActionListener(new ActionListener() {
            private boolean isDarkMode = false; // Track the current theme

            public void actionPerformed(ActionEvent e) {
                isDarkMode = !isDarkMode; // Toggle the theme

                if (isDarkMode) {
                    setDarkTheme(); // Set dark theme
                    themeMenuItem.setText("Set to Light"); // Update menu item text
                } else {
                    setLightTheme(); // Set light theme
                    themeMenuItem.setText("Set to Dark"); // Update menu item text
                }
            }

            private void setDarkTheme() {
                displayTxt.setBackground(Color.BLACK);
                displayTxt.setForeground(Color.WHITE);
            }

            private void setLightTheme() {
                displayTxt.setBackground(UIManager.getColor("TextArea.background"));
                displayTxt.setForeground(UIManager.getColor("TextArea.foreground"));
            }
        });
        
        shortcutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (activeShortcutsWindow == null) {
                    showShortcutsWindow();
                    shortcutMenuItem.setEnabled(false);
                }
            }
        });

        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(exitMenuItem);

        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);
        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.add(deleteMenuItem);
        editMenu.add(selectAllMenuItem);

        formatMenu.add(zoomInMenuItem);
        formatMenu.add(zoomOutMenuItem);
        
        viewMenu.add(themeMenuItem);
        viewMenu.add(shortcutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    private void registerKeyboardShortcuts() {
        // Save: Ctrl+S
        KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        displayTxt.getInputMap().put(saveKeyStroke, "save");
        displayTxt.getActionMap().put("save", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });

        // Save As: Ctrl+Shift+S
        KeyStroke saveAsKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S,
                KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        displayTxt.getInputMap().put(saveAsKeyStroke, "saveAs");
        displayTxt.getActionMap().put("saveAs", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                saveAsFile();
            }
        });

        // Open: Ctrl+O
        KeyStroke openKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        displayTxt.getInputMap().put(openKeyStroke, "open");
        displayTxt.getActionMap().put("open", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        // Exit: Ctrl+Esc
        KeyStroke exitKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, KeyEvent.CTRL_DOWN_MASK);
        displayTxt.getInputMap().put(exitKeyStroke, "exit");
        displayTxt.getActionMap().put("exit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        
        KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        displayTxt.getInputMap().put(undoKeyStroke, "undo");
        displayTxt.getActionMap().put("undo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });

        // Redo: Ctrl+Y
        KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);
        displayTxt.getInputMap().put(redoKeyStroke, "redo");
        displayTxt.getActionMap().put("redo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });

        // Zoom In: Ctrl+
        KeyStroke zoomInKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK);
        displayTxt.getInputMap().put(zoomInKeyStroke, "zoomIn");
        displayTxt.getActionMap().put("zoomIn", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                zoomIn();
            }
        });

        // Zoom Out: Ctrl-
        KeyStroke zoomOutKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK);
        displayTxt.getInputMap().put(zoomOutKeyStroke, "zoomOut");
        displayTxt.getActionMap().put("zoomOut", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                zoomOut();
            }
        });
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String content = Files.readString(Path.of(selectedFile.getAbsolutePath()));
                displayTxt.setText(content);
                currentFile = selectedFile; // Set the current file
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error opening file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        if (currentFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                writer.write(displayTxt.getText());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            saveAsFile(); // If no current file, fall back to Save As functionality
        }
    }

    private void saveAsFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                writer.write(displayTxt.getText());
                currentFile = selectedFile; // Update the current file
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exit() {
        dispose();
    }
    
    private void undo() {
        try {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } catch (CannotUndoException ex) {
            JOptionPane.showMessageDialog(this, "Unable to undo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void redo() {
        try {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        } catch (CannotRedoException ex) {
            JOptionPane.showMessageDialog(this, "Unable to redo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void zoomIn() {
        Font font = displayTxt.getFont();
        int size = font.getSize();
        Font newFont = font.deriveFont((float) (size + 1));
        displayTxt.setFont(newFont);
    }

    private void zoomOut() {
        Font font = displayTxt.getFont();
        int size = font.getSize();
        Font newFont = font.deriveFont((float) (size - 1));
        displayTxt.setFont(newFont);
    }
    
    private void showShortcutsWindow() {
        JFrame shortcutsFrame = new JFrame("Shortcuts");
        shortcutsFrame.setSize(300, 250);
        shortcutsFrame.setLocationRelativeTo(this);
        shortcutsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        String[] leftLabels = {"Open", "Save", "Save As", "Exit", "Undo", "Redo", "Cut", "Copy", "Paste", "Delete", "Select All", "Zoom In", "Zoom Out"};
        String[] rightLabels = {"Ctrl+O", "Ctrl+S", "Ctrl+Shift+S", "Ctrl+Esc", "Ctrl+Z", "Ctrl+Y", "Ctrl+X", "Ctrl+C", "Ctrl+V", "Del", "Ctrl+A", "Ctrl+", "Ctrl-"};

        for (int i = 0; i < leftLabels.length; i++) {
            JLabel leftLabel = new JLabel(leftLabels[i]);
            JLabel rightLabel = new JLabel(rightLabels[i]);
            
            // top, left, bottom, right
            leftLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Add left padding
            rightLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Add right padding


            panel.add(leftLabel);
            panel.add(rightLabel);
        }

        shortcutsFrame.add(panel);
        shortcutsFrame.setVisible(true);
        shortcutsFrame.setResizable(false);
        shortcutsFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                activeShortcutsWindow = null;
                enableShortcutsMenuItem();
            }
        });

        activeShortcutsWindow = shortcutsFrame;
    }

    private void enableShortcutsMenuItem() {
        JMenuBar menuBar = getJMenuBar();
        JMenu viewMenu = menuBar.getMenu(3); // Assuming "View" menu is at index 3
        JMenuItem shortcutMenuItem = viewMenu.getItem(1); // Assuming "Shortcuts" menu item is at index 1
        shortcutMenuItem.setEnabled(true);
    }
}
