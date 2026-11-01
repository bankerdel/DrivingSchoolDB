package org.example;

import java.io.IOException;
import java.time.LocalDate;

import org.example.dao.StudentFileManager;
import org.example.model.Student;

public class Benchmark {
    public static void main(String[] args) throws IOException {
        int[] sizes = {100, 1000, 10000};
        for (int n : sizes) {
            StudentFileManager manager = new StudentFileManager("benchmark.db");
            manager.clearDatabase();
            for (int i = 1; i < n; i++) {
                manager.addStudent(new Student(false, i, "Student_" + i, LocalDate.of(2000, 1, 1), 50000.0 + i, true));
            }
            long start = System.nanoTime();
            manager.addStudent(new Student(false, n, "Student_" + n, LocalDate.of(2000, 1, 1), 50000.0 + n, true));
            double addTime = (System.nanoTime() - start) / 1000.0;
            start = System.nanoTime();
            manager.findById(n);
            double findIdTime = (System.nanoTime() - start) / 1000.0;
            start = System.nanoTime();
            manager.findByFullname("Student_" + n);
            double findNameTime = (System.nanoTime() - start) / 1000.0;
            start = System.nanoTime();
            manager.findByCost(50000.0 + n);
            double findCostTime = (System.nanoTime() - start) / 1000.0;
            start = System.nanoTime();
            manager.deleteById(n);
            double deleteTime = (System.nanoTime() - start) / 1000.0;
            System.out.printf("N = %d | Add: %.2f us | FindID: %.2f us | FindName: %.2f us | FindCost(O(N)): %.2f us | Delete: %.2f us%n", n, addTime, findIdTime, findNameTime, findCostTime, deleteTime);
            manager.deleteDatabase();
        }
    }

}
