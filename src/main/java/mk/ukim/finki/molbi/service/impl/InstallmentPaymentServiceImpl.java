//package mk.ukim.finki.molbi.service.impl;
//
//import lombok.AllArgsConstructor;
//import mk.ukim.finki.molbi.model.base.Student;
//import mk.ukim.finki.molbi.model.requests.InstallmentPaymentStudentRequest;
//import mk.ukim.finki.molbi.model.enums.RequestType;
//import mk.ukim.finki.molbi.model.exceptions.InstallmentPaymentRequestNotFoundException;
//import mk.ukim.finki.molbi.model.exceptions.RequestSessionNotFoundException;
//import mk.ukim.finki.molbi.repository.InstallmentPaymentRepository;
//import mk.ukim.finki.molbi.repository.RequestSessionRepository;
//import mk.ukim.finki.molbi.service.inter.InstallmentPaymentService;
//import mk.ukim.finki.molbi.service.inter.StudentService;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//
//@Service
//@AllArgsConstructor
//public class InstallmentPaymentServiceImpl implements InstallmentPaymentService {
//
//    private final InstallmentPaymentRepository installmentPaymentRepository;
//    private final RequestSessionRepository requestSessionRepository;
//    private final StudentService studentService;
//
//    @Override
//    public Page<InstallmentPaymentStudentRequest> findByRequestSessionFilteredWithPagination(Long sessionId, int pageNum, int pageSize, FilterDto filter) {
//        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.Direction.ASC, "id");
//        if (filter.getNewStudyProgram() != null && filter.getIsProcessed() != null && filter.getStudent() != null) {
//            return installmentPaymentRepository.findAllFiltered(sessionId, filter.getIsApproved(), filter.getIsProcessed(), filter.getStudent(), RequestType.INSTALLMENT_PAYMENT, pageRequest);
//        } else {
//            return installmentPaymentRepository.findByRequestSession_Id(sessionId, pageRequest);
//        }
//
//    }
//
//    @Override
//    public void save(InstallmentPaymentDto dto) {
//        InstallmentPaymentStudentRequest request = new InstallmentPaymentStudentRequest();
//        request.setDescription(dto.getDescription());
//        Student student = studentService.findByIndex(dto.getStudent());
//        request.setStudent(student);
//        request.setDateCreated(LocalDate.now());
//        request.setRequestSession(requestSessionRepository
//                .findById(dto.getRequestSession())
//                .orElseThrow(() -> new RequestSessionNotFoundException(dto.getRequestSession()))
//        );
//        request.getRequestSession().setRequestType(RequestType.INSTALLMENT_PAYMENT);
//        request.setIsProcessed(Boolean.FALSE);
//        installmentPaymentRepository.save(request);
//    }
//
//    @Override
//    public InstallmentPaymentStudentRequest findById(Long id) {
//        return installmentPaymentRepository.findById(id).orElseThrow(() -> new InstallmentPaymentRequestNotFoundException(id));
//    }
//}
