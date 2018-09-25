
// Java Program to create a text editor using java 
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.metal.*;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

class Editor extends JFrame implements ActionListener {



    // undo and redo
    private Document editorPaneDocument;
    protected UndoHandler undoHandler = new UndoHandler();
    protected UndoManager undoManager = new UndoManager();
    private UndoAction undoAction = null;
    private RedoAction redoAction = null;


    // Text component 
    JTextArea t;

    // Frame 
    JFrame f;

    // Constructor 
    Editor()
    {


        // Create a frame 
        f = new JFrame("editor");

        try {
            // Set metl look and feel 
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

            // Set theme to ocean 
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        }
        catch (Exception e) {
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

        JMenuItem mc = new JMenuItem("close");

        mc.addActionListener(this);

        mb.add(m1);
        mb.add(m2);
        mb.add(mc);

        f.setJMenuBar(mb);
        f.add(t);
        f.setSize(500, 500);
        f.show();
    }

    // If a button is pressed 
    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();

        if (s.equals("cut")) {
            t.cut();
        }
        else if (s.equals("copy")) {
            t.copy();
        }
        else if (s.equals("paste")) {
            t.paste();
        }
        else if (s.equals("Save")) {
            // Create an object of JFileChooser class 
            JFileChooser j = new JFileChooser("f:");

            // Invoke the showsSaveDialog function to show the save dialog 
            int r = j.showSaveDialog(null);

            if (r == JFileChooser.APPROVE_OPTION) {

                // Set the label to the path of the selected directory 
                File fi = new File(j.getSelectedFile().getAbsolutePath());

                try {
                    // Create a file writer 
                    FileWriter wr = new FileWriter(fi, false);

                    // Create buffered writer to write 
                    BufferedWriter w = new BufferedWriter(wr);

                    // Write 
                    w.write(t.getText());

                    w.flush();
                    w.close();
                }
                catch (Exception evt) {
                    JOptionPane.showMessageDialog(f, evt.getMessage());
                }
            }
            // If the user cancelled the operation 
            else
                JOptionPane.showMessageDialog(f, "the user cancelled the operation");
        }
        else if (s.equals("Print")) {
            try {
                // print the file 
                t.print();
            }
            catch (Exception evt) {
                JOptionPane.showMessageDialog(f, evt.getMessage());
            }
        }
        else if (s.equals("Open")) {
            // Create an object of JFileChooser class 
            JFileChooser j = new JFileChooser("f:");

            // Invoke the showsOpenDialog function to show the save dialog 
            int r = j.showOpenDialog(null);

            // If the user selects a file 
            if (r == JFileChooser.APPROVE_OPTION) {
                // Set the label to the path of the selected directory 
                File fi = new File(j.getSelectedFile().getAbsolutePath());

                try {
                    // String 
                    String s1 = "", sl = "";

                    // File reader 
                    FileReader fr = new FileReader(fi);

                    // Buffered reader 
                    BufferedReader br = new BufferedReader(fr);

                    // Initilize sl 
                    sl = br.readLine();

                    // Take the input from the file 
                    while ((s1 = br.readLine()) != null) {
                        sl = sl + "\n" + s1;
                    }

                    // Set the text 
                    t.setText(sl);
                }
                catch (Exception evt) {
                    JOptionPane.showMessageDialog(f, evt.getMessage());
                }
            }
            // If the user cancelled the operation 
            else
                JOptionPane.showMessageDialog(f, "the user cancelled the operation");
        }
        else if (s.equals("New")) {
            t.setText("");
        }
        else if (s.equals("close")) {
            f.setVisible(false);
        }
    }

    // Main class 
    public static void main(String args[])
    {
        Editor e = new Editor();
    }

    // java undo and redo action classes

    class UndoHandler implements UndoableEditListener
    {

        /**
         * Messaged when the Document has created an edit, the edit is added to
         * <code>undoManager</code>, an instance of UndoManager.
         */
        public void undoableEditHappened(UndoableEditEvent e)
        {
            undoManager.addEdit(e.getEdit());
            undoAction.update();
            redoAction.update();
        }
    }

    class UndoAction extends AbstractAction
    {
        public UndoAction()
        {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                undoManager.undo();
            }
            catch (CannotUndoException ex)
            {
                // TODO deal with this
                //ex.printStackTrace();
            }
            update();
            redoAction.update();
        }

        protected void update()
        {
            if (undoManager.canUndo())
            {
                setEnabled(true);
                putValue(Action.NAME, undoManager.getUndoPresentationName());
            }
            else
            {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    class RedoAction extends AbstractAction
    {
        public RedoAction()
        {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                undoManager.redo();
            }
            catch (CannotRedoException ex)
            {
                // TODO deal with this
                ex.printStackTrace();
            }
            update();
            undoAction.update();
        }

        protected void update()
        {
            if (undoManager.canRedo())
            {
                setEnabled(true);
                putValue(Action.NAME, undoManager.getRedoPresentationName());
            }
            else
            {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

}




