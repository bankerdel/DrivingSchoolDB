import org.example.dao.StudentFileManager;
import org.example.gui.MainFrame;
import org.example.model.Student;
import javax.swing.SwingUtilities;

import javax.swing.*;


public static void main(String[] args){
    try (RandomAccessFile raf = new RandomAccessFile("students.db", "rw")) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
    catch (IOException e) {
        e.printStackTrace();
    }
}
