package com.example.demo.service;

import com.example.demo.model.EventParticipation;
import com.example.demo.model.Student;
import com.example.demo.repository.EventParticipationRepository;
import com.example.demo.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EventParticipationServiceImpl implements EventParticipationService {

    private final EventParticipationRepository eventParticipationRepository;
    private final StudentRepository studentRepository;

    public EventParticipationServiceImpl(EventParticipationRepository eventParticipationRepository, StudentRepository studentRepository) {
        this.eventParticipationRepository = eventParticipationRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public EventParticipation save(EventParticipation participation) {
        if (participation.getRollNumber() != null) {
            Optional<Student> studentOpt = studentRepository.findByRollNumberIgnoreCase(participation.getRollNumber().trim());
            studentOpt.ifPresent(participation::setStudent);
        }
        if (participation.getDepartment() != null) {
            participation.setDepartment(com.example.demo.util.DepartmentNormalizer.normalize(participation.getDepartment().trim()));
        }
        return eventParticipationRepository.save(participation);
    }

    @Override
    public List<EventParticipation> getReport(String category, LocalDate startDate, LocalDate endDate) {
        if (category == null || category.trim().isEmpty() || "All".equalsIgnoreCase(category.trim())) {
            return eventParticipationRepository.findByEventDateBetween(startDate, endDate);
        }
        return eventParticipationRepository.findByEventCategoryAndEventDateBetween(category.trim(), startDate, endDate);
    }

    @Override
    public List<EventParticipation> getByRollNumber(String rollNumber) {
        return eventParticipationRepository.findByRollNumberIgnoreCase(rollNumber);
    }

    @Override
    public List<EventParticipation> getAll() {
        return eventParticipationRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        eventParticipationRepository.deleteById(id);
    }

    @Override
    public Optional<EventParticipation> getById(Long id) {
        return eventParticipationRepository.findById(id);
    }
}
