package org.example.gui;

import org.example.dao.StudentFileManager;
import org.example.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class MainFrame extends JFrame {
    private StudentFileManager fileManager;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField idField, nameField, birthDateField, costField;
    private JCheckBox activeCheckBox;

    public MainFrame() {
        setTitle("База данных учеников автошколы");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        try {
            this.fileManager = new StudentFileManager("students.db");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String[] columns = {"ID", "ФИО", "Дата рождения (ГГГГ-ММ-ДД)", "Стоимость", "Статус"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1));
        JPanel inputPanel = new JPanel();
        idField = new JTextField(5);
        nameField = new JTextField(15);
        birthDateField = new JTextField(10);
        costField = new JTextField(8);
        activeCheckBox = new JCheckBox("Активен", true);
        inputPanel.add(new JLabel("ID:"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel("ФИО:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Дата (ГГГГ-ММ-ДД):"));
        inputPanel.add(birthDateField);
        inputPanel.add(new JLabel("Стоимость:"));
        inputPanel.add(costField);
        inputPanel.add(activeCheckBox);
        bottomPanel.add(inputPanel);
        JPanel actionPanel = new JPanel();
        JButton addBtn = new JButton("Добавить");
        JButton updateBtn = new JButton("Редактировать по ID");
        JButton showAllBtn = new JButton("Показать всех");
        actionPanel.add(addBtn);
        actionPanel.add(updateBtn);
        actionPanel.add(showAllBtn);
        bottomPanel.add(actionPanel);
        add(bottomPanel, BorderLayout.SOUTH);
        JPanel searchDeletePanel = new JPanel();
        JButton findByIdBtn = new JButton("Найти по ID");
        JButton findByNameBtn = new JButton("Найти по ФИО");
        JButton deleteByIdBtn = new JButton("Удалить по ID");
        JButton deleteByNameBtn = new JButton("Удалить по ФИО");
        bottomPanel.add(searchDeletePanel);
        String[] searchFields = {"По ID", "По ФИО", "По Дате", "По Стоимости", "По Статусу"};
        JComboBox<String> searchBox = new JComboBox<>(searchFields);
        JButton searchBtn = new JButton("Найти");
        searchDeletePanel.add(new JLabel("Поиск: "));
        searchDeletePanel.add(searchBox);
        searchDeletePanel.add(searchBtn);
        searchDeletePanel.add(deleteByIdBtn);
        searchDeletePanel.add(deleteByNameBtn);
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        showAllBtn.addActionListener(e -> refreshTable());
        searchBtn.addActionListener(e -> {try {
            List<Student> found = new ArrayList<>();
            int selected = searchBox.getSelectedIndex();
            switch (selected) {
                case 0:
                    int id = Integer.parseInt(idField.getText().trim());
                    Student s = fileManager.findById(id);
                    if (s != null) found.add(s);
                    break;
                case 1:
                    found = fileManager.findByFullname(nameField.getText().trim());
                    break;
                case 2:
                    found = fileManager.findByBirthDate(LocalDate.parse(birthDateField.getText().trim()));
                    break;
                case 3:
                    found = fileManager.findByCost(Double.parseDouble(costField.getText().trim()));
                    break;
                case 4:
                    found = fileManager.findByActive(activeCheckBox.isSelected());
                    break;
            }
            tableModel.setRowCount(0);
            if (found.isEmpty()) JOptionPane.showMessageDialog(this, "Записи не найдены!");
            else {
                for (Student s : found) {
                    fileManager.addStudent(s);
                }
                refreshTable();
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage());}
        });
        addBtn.addActionListener(e -> {try {
            Student s = readStudentFromFields();
            if (fileManager.addStudent(s)) refreshTable();
            else JOptionPane.showMessageDialog(this, "Ученик с таким ID уже существует!");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage());}
        });
        updateBtn.addActionListener(e -> {try {
            Student s = readStudentFromFields();
            if (fileManager.updateStudent(s)) refreshTable();
            else JOptionPane.showMessageDialog(this, "Ученик с таким ID не найден!");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage());}
        });

        deleteByIdBtn.addActionListener(e -> {try {
            int id = Integer.parseInt(idField.getText().trim());
            fileManager.deleteById(id);
            refreshTable();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage());}
        });
        deleteByNameBtn.addActionListener(e -> {try {
            String name = nameField.getText().trim();
            int count = fileManager.deleteByFullname(name);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Удалено записей: " + count);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage());}
        });
        refreshTable();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        JButton openDbBtn = new JButton("Открыть/Создать БД");
        JButton clearDbBtn = new JButton("Очистить БД");
        JButton deleteDbBtn = new JButton("Удалить БД");
        JButton backupBtn = new JButton("Создать Backup");
        JButton restoreBtn = new JButton("Восстановить Backup");
        JButton excelBtn = new JButton("Экспорт в Excel");
        topPanel.add(openDbBtn);
        topPanel.add(clearDbBtn);
        topPanel.add(deleteDbBtn);
        topPanel.add(backupBtn);
        topPanel.add(restoreBtn);
        topPanel.add(excelBtn);
        clearDbBtn.addActionListener(e -> {try {
            fileManager.clearDatabase();
            refreshTable();
            JOptionPane.showMessageDialog(this, "База данных очищена!");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage());}
        });
        deleteDbBtn.addActionListener(e -> {try {
            if (fileManager.deleteDatabase()) {
                refreshTable();
                JOptionPane.showMessageDialog(this, "Файл базы данных удален!");
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage());}
        });
        backupBtn.addActionListener(e -> {try {
            File backupFile = new File(fileManager.getDbFileName() + ".bak");
            fileManager.createBackup(backupFile);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Backup создан: " + backupFile.getName());
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage());}
        });
        restoreBtn.addActionListener(e -> {try {
            File backupFile = new File(fileManager.getDbFileName() + ".bak");
            fileManager.restoreFromBackup(backupFile);
            refreshTable();
            JOptionPane.showMessageDialog(this, "БД восстановлена из Backup!");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage());}
        });
        excelBtn.addActionListener(e -> {try {
            File excelFile = new File(fileManager.getDbFileName() + ".xlsx");
            fileManager.exportToExcel(excelFile);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Экспортировано в " + excelFile.getName());
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage());}
        });
        openDbBtn.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(this, "Введите имя файла БД:", "students.db");
            if (fileName != null && !fileName.trim().isEmpty()) {
                try {
                    this.fileManager = new StudentFileManager(fileName.trim());
                    refreshTable();
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage());}
            }
        });
        return topPanel;
    }

    private Student readStudentFromFields() {
        int id = Integer.parseInt(idField.getText().trim());
        String name = nameField.getText().trim();
        LocalDate date = LocalDate.parse(birthDateField.getText().trim());
        double cost = Double.parseDouble(costField.getText().trim());
        boolean active = activeCheckBox.isSelected();
        return new Student(false, id, name, date, cost, active);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            if (fileManager != null) {
                List<Student> students = fileManager.getAllStudents();
                for (Student s : students) {
                    tableModel.addRow(new Object[]{s.getId(), s.getFullname(), s.getBirthDate(), s.getCost(), s.isActive()});
                }
            }
        } catch (IOException e) { JOptionPane.showMessageDialog(this, "Ошибка чтения базы данных: " + e.getMessage()); }
    }
}
