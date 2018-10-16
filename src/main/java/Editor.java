
// Java Program to create a text editor using java

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.metal.*;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

class Editor extends JFrame implements ActionListener {

    final static byte[] SAFETY = {25, -128, 45, 32, 24, 52, 100, -74};
    final static byte[] KEY = {100, 100, 100, 100, 100, 100, 100, 100};
    final static byte[] IV = {123, 43, 42, -15, -53, 117, 7, 41, 29, 42, 42, 18, -120, 14, 29, -58};

    // undo and redo
    private Document editorPaneDocument;
    protected UndoHandler undoHandler = new UndoHandler();
    protected UndoManager undoManager = new UndoManager();
    private UndoAction undoAction = null;
    private RedoAction redoAction = null;

    private Crypto crypto;
    private String keyFilePath = "none";
    private JMenu m4;

    // Text component 
    JTextArea t;

    // Frame 
    JFrame f;

    // Constructor 
    Editor() {
        crypto = new Crypto();
        crypto.setKey(KEY);
        crypto.setIv(IV);

        // Create a frame 
        f = new JFrame("editor");

        try {
            // Set metl look and feel 
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

            // Set theme to ocean 
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        } catch (Exception e) {
        }

        // Text component 
        t = new JTextArea();

        // https://alvinalexander.com/java/java-undo-redo
        editorPaneDocument = t.getDocument();
        editorPaneDocument.addUndoableEditListener(undoHandler);

        // https://stackoverflow.com/questions/4465869/how-do-i-implement-ctrlz-commandz-in-java-swing/4465932
        KeyStroke undoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        KeyStroke redoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

        undoAction = new UndoAction();
        t.getInputMap().put(undoKeystroke, "undoKeystroke");
        t.getActionMap().put("undoKeystroke", undoAction);

        redoAction = new RedoAction();
        t.getInputMap().put(redoKeystroke, "redoKeystroke");
        t.getActionMap().put("redoKeystroke", redoAction);



        // Create a menubar
        JMenuBar mb = new JMenuBar();

        // Create amenu for menu 
        JMenu m1 = new JMenu("File");

        // Create menu items 
        JMenuItem mi1 = new JMenuItem("New");
        JMenuItem mi2 = new JMenuItem("Open");
        JMenuItem mi3 = new JMenuItem("Save");
        JMenuItem mi9 = new JMenuItem("Print");

        // Add action listener 
        mi1.addActionListener(this);
        mi2.addActionListener(this);
        mi3.addActionListener(this);
        mi9.addActionListener(this);

        m1.add(mi1);
        m1.add(mi2);
        m1.add(mi3);
        m1.add(mi9);

        // Create amenu for menu 
        JMenu m2 = new JMenu("Edit");

        // Create menu items 
        JMenuItem mi4 = new JMenuItem("cut");
        JMenuItem mi5 = new JMenuItem("copy");
        JMenuItem mi6 = new JMenuItem("paste");


        // Edit menu
        JMenuItem undoMenuItem = new JMenuItem(undoAction);
        JMenuItem redoMenuItem = new JMenuItem(redoAction);
        m2.add(undoMenuItem);
        m2.add(redoMenuItem);

        // Add action listener 
        mi4.addActionListener(this);
        mi5.addActionListener(this);
        mi6.addActionListener(this);

        m2.add(mi4);
        m2.add(mi5);
        m2.add(mi6);

        JMenu m3 = new JMenu("Key");
        JMenuItem mi7 = new JMenuItem("Load");
        mi7.addActionListener(this);
        m3.add(mi7);

        m4 = new JMenu("key: " + keyFilePath);

        JMenuItem mc = new JMenuItem("close");

        mc.addActionListener(this);

        mb.add(m1);
        mb.add(m2);
        mb.add(m3);
        mb.add(m4);
        mb.add(mc);

        f.setJMenuBar(mb);
        f.add(t);
        f.setSize(500, 500);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.setVisible(true);

        f.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/check.png")));
    }

    // If a button is pressed 
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();

        if (s.equals("cut")) {
            t.cut();
        } else if (s.equals("copy")) {
            t.copy();
        } else if (s.equals("paste")) {
            t.paste();
        } else if (s.equals("Save")) {
            // Create an object of JFileChooser class 
            JFileChooser j = new JFileChooser("f:");

            // Invoke the showsSaveDialog function to show the save dialog 
            int r = j.showSaveDialog(null);

            if (r == JFileChooser.APPROVE_OPTION) {

                // Set the label to the path of the selected directory 
                File fi = new File(j.getSelectedFile().getAbsolutePath());

                try (FileInputStream fr = new FileInputStream(fi); FileOutputStream fw = new FileOutputStream(fi)) {

                    // Read cypertext from file
                    byte[] fullBytes = IOUtils.toByteArray(fr);

                    int errorCode;
                    if (fullBytes.length != 0 && (errorCode = fileIsSafe(fi)) != 0) {
                        if(errorCode == -1) JOptionPane.showMessageDialog(f, "This file is not meant for editing with this software");
                        if(errorCode == -2) JOptionPane.showMessageDialog(f, "Incorrect key for this file");
                        return;
                    }

                    // Encrypt to cyphertext
                    byte[] encodedBytes = crypto.encrypt(ArrayUtils.addAll(SAFETY, Crypto.getUTF8Bytes(t.getText())));

                    byte[] finalBytes = ArrayUtils.addAll(SAFETY, encodedBytes);

                    // Write
                    FileUtils.writeByteArrayToFile(fi, finalBytes);

                    return;

                } catch (Exception evt) {
                    JOptionPane.showMessageDialog(f, evt.getMessage());
                }
            }
            // If the user cancelled the operation 
            else
                JOptionPane.showMessageDialog(f, "the user cancelled the operation");
        } else if (s.equals("Print")) {
            try {
                // print the file 
                t.print();
            } catch (Exception evt) {
                JOptionPane.showMessageDialog(f, evt.getMessage());
            }
        } else if (s.equals("Open")) {
            // Create an object of JFileChooser class 
            JFileChooser j = new JFileChooser("f:");

            // Invoke the showsOpenDialog function to show the save dialog 
            int r = j.showOpenDialog(null);

            // If the user selects a file 
            if (r == JFileChooser.APPROVE_OPTION) {
                // Set the label to the path of the selected directory 
                File fi = new File(j.getSelectedFile().getAbsolutePath());

                try (FileInputStream fr = new FileInputStream(fi)) {

                    byte[] fullBytes = IOUtils.toByteArray(fr);

                    if (fileIsSafe(fi) != 0) {
                        JOptionPane.showMessageDialog(f, "This file is not meant for editing with this software");
                        return;
                    }

                    byte[] cypherText = Arrays.copyOfRange(fullBytes, SAFETY.length, fullBytes.length);

                    // Decrypt
                    System.out.println(cypherText.length);
                    String result = new String(crypto.decrypt(cypherText), StandardCharsets.UTF_8);

                    t.setText(result.substring(SAFETY.length, result.length()));

                } catch (Exception evt) {
                    JOptionPane.showMessageDialog(f, evt.getMessage());
                }
            }
            // If the user cancelled the operation 
            else
                JOptionPane.showMessageDialog(f, "the user cancelled the operation");
        } else if (s.equals("New")) {
            t.setText("");
        } else if (s.equals("close")) {
            f.setVisible(false);
        } else if (s.equals("Load")) {
            JFileChooser j = new JFileChooser("f:");

            // Invoke the showsOpenDialog function to show the save dialog
            int r = j.showOpenDialog(null);

            // If the user selects a file
            if (r == JFileChooser.APPROVE_OPTION) {
                // Set the label to the path of the selected directory
                File fi = new File(j.getSelectedFile().getAbsolutePath());

                try (FileInputStream fr = new FileInputStream(fi)) {

                    byte[] fullBytes = IOUtils.toByteArray(fr);

                    this.keyFilePath = j.getSelectedFile().getName();
                    m4.setText("key: " + this.keyFilePath);
                    crypto.setKey(fullBytes);

                } catch (Exception evt) {
                    JOptionPane.showMessageDialog(f, evt.getMessage());
                }
            }
        }
    }

    public int fileIsSafe(File fi) throws Exception {

        try (FileInputStream fr = new FileInputStream(fi)) {

            byte[] fullBytes = IOUtils.toByteArray(fr);

            System.out.println(Arrays.toString(fullBytes));

            if (fullBytes.length < SAFETY.length) {
                return -1;
            }

            byte[] safetyBytes = Arrays.copyOfRange(fullBytes, 0, SAFETY.length);

            if (!Arrays.equals(safetyBytes, SAFETY)) {
                return -1;
            }

            byte[] encodedBytes = Arrays.copyOfRange(fullBytes, SAFETY.length, fullBytes.length);

            byte[] decoded = crypto.decrypt(encodedBytes);

            System.out.println(Arrays.toString(decoded));

            if(decoded.length < SAFETY.length) {
                return -2;
            }

            safetyBytes = Arrays.copyOfRange(decoded, 0, SAFETY.length);

            if(!Arrays.equals(SAFETY, safetyBytes)) {
                return -2;
            }
        }

        return 0;
    }

    // Main class 
    public static void main(String args[]) {
        Editor e = new Editor();
    }

    // java undo and redo action classes

    class UndoHandler implements UndoableEditListener {

        /**
         * Messaged when the Document has created an edit, the edit is added to
         * <code>undoManager</code>, an instance of UndoManager.
         */
        public void undoableEditHappened(UndoableEditEvent e) {
            undoManager.addEdit(e.getEdit());
            undoAction.update();
            redoAction.update();
        }
    }

    class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undoManager.undo();
            } catch (CannotUndoException ex) {
                // TODO deal with this
                //ex.printStackTrace();
            }
            update();
            redoAction.update();
        }

        protected void update() {
            if (undoManager.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undoManager.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undoManager.redo();
            } catch (CannotRedoException ex) {
                // TODO deal with this
                ex.printStackTrace();
            }
            update();
            undoAction.update();
        }

        protected void update() {
            if (undoManager.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undoManager.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

}




