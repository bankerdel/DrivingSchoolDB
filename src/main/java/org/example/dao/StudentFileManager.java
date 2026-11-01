package org.example.dao;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.Student;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.nio.file.Files;

public class StudentFileManager {
    private File dbFile;
    private final Map<Integer, Long> indexMap = new HashMap<>();
    private final Queue<Long> freeOffsets = new ArrayDeque<>();
    private final Map<String, Set<Long>> nameIndex = new HashMap<>();

    public StudentFileManager(String filePath) throws IOException {
        this.dbFile = new File(filePath);
        loadDatabase();
    }

    public void loadDatabase() throws IOException {
        indexMap.clear(); nameIndex.clear(); freeOffsets.clear();
        if (!dbFile.exists()) {
            dbFile.createNewFile();
            return;
        }
        else {
            try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
                for (long offset = 0; offset < raf.length(); offset += Student.RECORD_SIZE) {
                    Student s = readRecord(raf, offset);
                    if (s.isDeleted()) {
                        freeOffsets.add(offset);
                    }
                    else {
                        indexMap.put(s.getId(), offset);
                        nameIndex.computeIfAbsent(s.getFullname(), k -> new HashSet<>()).add(offset);
                    }
                }
            }
        }
    }

    public String getDbFileName() {
        return dbFile.getName();
    }

    public List<Student> findByFullname(String fullname) throws IOException {
        List<Student> result = new ArrayList<>();
        Set<Long> offsets = nameIndex.get(fullname);
        if (offsets == null || offsets.isEmpty()) {
            return result;
        }
        else {
            try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
                for (Long offset : offsets) {
                    result.add(readRecord(raf, offset));
                }
            }
            return result;
        }
    }

    public int deleteByFullname (String fullname) throws IOException {
        Set<Long> offsets = nameIndex.remove(fullname);
        if (offsets == null || offsets.isEmpty()) {
            return 0;
        }
        else {
            try (RandomAccessFile raf = new RandomAccessFile(dbFile, "rw")) {
                for (Long offset : offsets) {
                    Student s = readRecord(raf, offset);
                    indexMap.remove(s.getId());
                    raf.seek(offset);
                    raf.writeBoolean(true);
                    freeOffsets.add(offset);
                }
            }
            return offsets.size();
        }
    }

    public boolean updateStudent(Student updatedStudent) throws IOException {
        Long offset = indexMap.get(updatedStudent.getId());
        if (offset == null) {
            return false;
        }
        else {
            try (RandomAccessFile raf = new RandomAccessFile(dbFile, "rw")) {
                Student old = readRecord(raf, offset);
                if (!old.getFullname().equals(updatedStudent.getFullname())) {
                    Set<Long> oldSet = nameIndex.get(old.getFullname());
                    if (oldSet != null) oldSet.remove(offset);
                    nameIndex.computeIfAbsent(updatedStudent.getFullname(), k -> new HashSet<>()).add(offset);
                }
                writeRecord(raf, offset, updatedStudent);
            }
            return true;
        }
    }

    public List<Student> getAllStudents() throws IOException {
        List<Student> list = new ArrayList<>();
        if (!dbFile.exists()) return list;
        else {
            try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
                for (Long offset : indexMap.values()) {
                    list.add(readRecord(raf, offset));
                }
            }
            return list;
        }
    }

    public void writeRecord(RandomAccessFile raf, long offset, Student student) throws IOException {
        raf.seek(offset);
        raf.writeBoolean(student.isDeleted());
        raf.writeInt(student.getId());
        for (int i = 0; i < Student.NAME_LENGTH; i++) {
            if (i < student.getFullname().length()) {
                raf.writeChar(student.getFullname().charAt(i));
            }
            else {
                raf.writeChar(' ');
            }
        }
        raf.writeLong(student.getBirthDate().toEpochDay());
        raf.writeDouble(student.getCost());
        raf.writeBoolean(student.isActive());
    }
    public Student readRecord(RandomAccessFile raf, long offset) throws IOException{
        raf.seek(offset);
        boolean deleted = raf.readBoolean();
        int id = raf.readInt();
        char[] chars = new char[Student.NAME_LENGTH];
        for (int i = 0; i < Student.NAME_LENGTH; i++) {
            chars[i] = raf.readChar();
        }
        String fullname = new String(chars).trim();
        long epochDay = raf.readLong();
        LocalDate birthDate = LocalDate.ofEpochDay(epochDay);
        double cost = raf.readDouble();
        boolean active = raf.readBoolean();
        return new Student(deleted, id, fullname, birthDate, cost, active);
    }

    public boolean addStudent(Student student) throws IOException {
        if (indexMap.containsKey(student.getId())) {
            return false;
        }
        long offset;
        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "rw")) {
            if (!freeOffsets.isEmpty()) {
                offset = freeOffsets.poll();
            }
            else {
                offset = raf.length();
            }
            writeRecord(raf, offset, student);
            indexMap.put(student.getId(), offset);
            nameIndex.computeIfAbsent(student.getFullname(), k -> new HashSet<>()).add(offset);
            return true;
        }
    }

    public Student findById(int id) throws IOException {
        Long offset = indexMap.get(id);
        if (offset == null) {
            return null;
        }
        else {
            try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
                return readRecord(raf, offset);
            }
        }
    }

    public boolean deleteById(int id) throws IOException {
        Long offset = indexMap.remove(id);
        if (offset == null) {
            return false;
        }
        else {
            try (RandomAccessFile raf = new RandomAccessFile(dbFile, "rw")) {
                Student s = readRecord(raf, offset);
                Set<Long> set = nameIndex.get(s.getFullname());
                if (set != null) set.remove(offset);
                raf.seek(offset);
                raf.writeBoolean(true);
                freeOffsets.add(offset);
                return true;
            }
        }
    }

    public List<Student> findByBirthDate(LocalDate date) throws IOException {
        List<Student> list = new ArrayList<>();
        if (!dbFile.exists()) return list;
        else {
            try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
                for (Long offset : indexMap.values()) {
                    Student s = readRecord(raf, offset);
                    if (s.getBirthDate().equals(date)) list.add(s);
                }
            }
            return list;
        }
    }

    public List<Student> findByCost(double cost) throws IOException {
        List<Student> list = new ArrayList<>();
        if (!dbFile.exists()) return list;
        else {
            try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
                for (Long offset : indexMap.values()) {
                    Student s = readRecord(raf, offset);
                    if (Double.compare(s.getCost(), cost) == 0) list.add(s);
                }
            }
            return list;
        }
    }

    public List<Student> findByActive(boolean active) throws IOException {
        List<Student> list = new ArrayList<>();
        if (!dbFile.exists()) return list;
        else {
            try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
                for (Long offset : indexMap.values()) {
                    Student s = readRecord(raf, offset);
                    if (s.isActive() == active) list.add(s);
                }
            }
            return list;
        }
    }

    public void clearDatabase() throws IOException {
        indexMap.clear(); nameIndex.clear(); freeOffsets.clear();
        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "rw")) {
            raf.setLength(0);
        }
    }

    public boolean deleteDatabase() throws IOException {
        indexMap.clear(); nameIndex.clear(); freeOffsets.clear();
        return dbFile.delete();
    }

    public void createBackup(File backupFile) throws IOException {
        Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public void restoreFromBackup(File backupFile) throws IOException {
        Files.copy(backupFile.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        loadDatabase();
    }

    public void exportToExcel(File excelFile) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(excelFile)) {
            Sheet sheet = workbook.createSheet("Ученики");
            Row head =  sheet.createRow(0);
            head.createCell(0).setCellValue("ID");
            head.createCell(1).setCellValue("ФИО");
            head.createCell(2).setCellValue("Дата рождения");
            head.createCell(3).setCellValue("Стоимость обучения");
            head.createCell(4).setCellValue("Статус");
            List<Student> students = getAllStudents();
            int rowNum = 1;
            for (Student student : students) {
                Row stRow = sheet.createRow(rowNum++);
                stRow.createCell(0).setCellValue(student.getId());
                stRow.createCell(1).setCellValue(student.getFullname());
                stRow.createCell(2).setCellValue(student.getBirthDate().toString());
                stRow.createCell(3).setCellValue(student.getCost());
                stRow.createCell(4).setCellValue(student.isActive());
            }
            workbook.write(fos);
        }
    }
}
