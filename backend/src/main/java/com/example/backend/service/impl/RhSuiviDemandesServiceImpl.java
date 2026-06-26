package com.example.backend.service.impl;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.demande.DemandeCongeImpressionResponse;
import com.example.backend.dto.rh.RhDemandeSuiviResponse;
import com.example.backend.dto.rh.RhDepartementResponse;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.RhDemandeSuiviMapper;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.repository.DepartementRepository;
import com.example.backend.repository.specification.DemandeCongeSpecifications;
import com.example.backend.service.interfaces.IDemandeCongeImpressionService;
import com.example.backend.service.interfaces.IRhSuiviDemandesService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RhSuiviDemandesServiceImpl implements IRhSuiviDemandesService {

    private static final int DEFAULT_SIZE = 4;
    private static final int MAX_SIZE = 20;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DemandeCongeRepository demandeCongeRepository;
    private final DepartementRepository departementRepository;
    private final EmployeConnecteProvider employeConnecteProvider;
    private final IDemandeCongeImpressionService impressionService;
    private final RhDemandeSuiviMapper mapper;

    public RhSuiviDemandesServiceImpl(
            DemandeCongeRepository demandeCongeRepository,
            DepartementRepository departementRepository,
            EmployeConnecteProvider employeConnecteProvider,
            IDemandeCongeImpressionService impressionService,
            RhDemandeSuiviMapper mapper) {
        this.demandeCongeRepository = demandeCongeRepository;
        this.departementRepository = departementRepository;
        this.employeConnecteProvider = employeConnecteProvider;
        this.impressionService = impressionService;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RhDemandeSuiviResponse> getCongesValidesDg(
            int page,
            int size,
            Integer annee,
            Integer mois,
            String search,
            Long departementId) {
        return getDemandes(TypeDemande.CONGE, page, size, annee, mois, search, departementId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RhDemandeSuiviResponse> getAbsencesValideesDg(
            int page,
            int size,
            Integer annee,
            Integer mois,
            String search,
            Long departementId) {
        return getDemandes(TypeDemande.ABSENCE, page, size, annee, mois, search, departementId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCongesValidesDgExcel(Integer annee, Integer mois, String search, Long departementId) {
        return exportDemandes(TypeDemande.CONGE, annee, mois, search, departementId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAbsencesValideesDgExcel(Integer annee, Integer mois, String search, Long departementId) {
        return exportDemandes(TypeDemande.ABSENCE, annee, mois, search, departementId);
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeCongeImpressionResponse imprimerCongeValideDg(Long demandeId) {
        return imprimerDemandeValideeDg(demandeId, TypeDemande.CONGE);
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeCongeImpressionResponse imprimerAbsenceValideeDg(Long demandeId) {
        return imprimerDemandeValideeDg(demandeId, TypeDemande.ABSENCE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RhDepartementResponse> getDepartements() {
        verifierRhConnecte();
        return departementRepository.findAll().stream()
                .map(departement -> new RhDepartementResponse(departement.getId(), departement.getNom()))
                .toList();
    }

    private DemandeCongeImpressionResponse imprimerDemandeValideeDg(Long demandeId, TypeDemande typeDemande) {
        verifierRhConnecte();
        DemandeConge demande = demandeCongeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable"));

        if (demande.getTypeDemande() != typeDemande) {
            throw new InvalidBusinessRequestException("Type de demande invalide pour cette impression");
        }
        if (demande.getStatus() != StatusDemande.VALIDE_DG) {
            throw new InvalidBusinessRequestException(
                    "Seules les demandes validees par le directeur general peuvent etre imprimees");
        }

        return impressionService.getDemandePourImpression(demandeId);
    }

    private PageResponse<RhDemandeSuiviResponse> getDemandes(
            TypeDemande typeDemande,
            int page,
            int size,
            Integer annee,
            Integer mois,
            String search,
            Long departementId) {
        verifierRhConnecte();
        DateRange dateRange = resolveDateRange(annee, mois);
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        Specification<DemandeConge> spec = buildRhSuiviSpecification(typeDemande, dateRange, search, departementId);

        Page<RhDemandeSuiviResponse> result = demandeCongeRepository.findAll(spec, pageable)
                .map(mapper::toResponse);

        return PageResponse.from(result);
    }

    private byte[] exportDemandes(
            TypeDemande typeDemande,
            Integer annee,
            Integer mois,
            String search,
            Long departementId) {
        verifierRhConnecte();
        DateRange dateRange = resolveDateRange(annee, mois);
        Specification<DemandeConge> spec = buildRhSuiviSpecification(typeDemande, dateRange, search, departementId);
        List<RhDemandeSuiviResponse> demandes = demandeCongeRepository.findAll(
                        spec,
                        Sort.by(Sort.Direction.DESC, "dateDebutDg").and(Sort.by(Sort.Direction.DESC, "updatedAt")))
                .stream()
                .map(mapper::toResponse)
                .toList();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(typeDemande == TypeDemande.CONGE ? "Conges valides DG" : "Absences validees DG");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper helper = workbook.getCreationHelper();
            dateStyle.setDataFormat(helper.createDataFormat().getFormat("dd/mm/yyyy"));

            String[] headers = headers(typeDemande);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < demandes.size(); i++) {
                writeRow(sheet.createRow(i + 1), demandes.get(i), typeDemande);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new InvalidBusinessRequestException("Erreur lors de l'export Excel.");
        }
    }

    private void writeRow(Row row, RhDemandeSuiviResponse demande, TypeDemande typeDemande) {
        row.createCell(0).setCellValue(value(demande.reference()));
        row.createCell(1).setCellValue(value(demande.matricule()));
        row.createCell(2).setCellValue(value(demande.employeNomComplet()));
        row.createCell(3).setCellValue(value(demande.email()));
        row.createCell(4).setCellValue(value(demande.departementNom()));
        row.createCell(5).setCellValue(value(demande.typeDemande()));
        row.createCell(6).setCellValue(value(demande.natureConge()));
        row.createCell(7).setCellValue(formatDate(demande.dateDebutDg()));
        row.createCell(8).setCellValue(formatDate(demande.dateFinDg()));
        row.createCell(9).setCellValue(demande.joursDeduits() == null ? 0D : demande.joursDeduits());
        row.createCell(10).setCellValue(value(demande.status()));
        row.createCell(11).setCellValue(demande.createdAt() == null ? "" : demande.createdAt().toLocalDate().format(DATE_FORMAT));
    }

    private String[] headers(TypeDemande typeDemande) {
        return new String[] {
                "Reference",
                "Matricule",
                "Employe",
                "Email",
                "Departement",
                "Type",
                typeDemande == TypeDemande.CONGE ? "Nature" : "Nature/Motif",
                "Date debut DG",
                "Date fin DG",
                typeDemande == TypeDemande.CONGE ? "Jours deduits du solde conge" : "Jours a deduire de la paie",
                "Statut",
                "Date creation"
        };
    }

    private void verifierRhConnecte() {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        Role role = employe.getUtilisateur() == null ? null : employe.getUtilisateur().getRole();
        if (role != Role.RH) {
            throw new ForbiddenException("Acces reserve au service RH.");
        }
    }

    private int normalizePage(int page) {
        if (page < 0) {
            throw new InvalidBusinessRequestException("Parametres de pagination invalides.");
        }
        return page;
    }

    private int normalizeSize(int size) {
        int normalizedSize = size <= 0 ? DEFAULT_SIZE : size;
        if (normalizedSize > MAX_SIZE) {
            throw new InvalidBusinessRequestException("Parametres de pagination invalides.");
        }
        return normalizedSize;
    }

    private String normalizeSearchPattern(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }
        return "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private Specification<DemandeConge> buildRhSuiviSpecification(
            TypeDemande typeDemande,
            DateRange dateRange,
            String search,
            Long departementId) {
        return DemandeCongeSpecifications.rhSuiviDemandes(
                typeDemande,
                StatusDemande.VALIDE_DG,
                dateRange.start(),
                dateRange.endExclusive(),
                normalizeSearchPattern(search),
                departementId);
    }

    private DateRange resolveDateRange(Integer annee, Integer mois) {
        if (mois != null && (mois < 1 || mois > 12)) {
            throw new InvalidBusinessRequestException("Mois invalide.");
        }
        if (annee == null && mois != null) {
            throw new InvalidBusinessRequestException("L'annee est obligatoire lorsque le mois est renseigne.");
        }
        if (annee == null) {
            return new DateRange(null, null);
        }
        if (mois == null) {
            return new DateRange(LocalDate.of(annee, 1, 1), LocalDate.of(annee + 1, 1, 1));
        }
        LocalDate start = LocalDate.of(annee, mois, 1);
        return new DateRange(start, start.plusMonths(1));
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMAT);
    }

    private record DateRange(LocalDate start, LocalDate endExclusive) {
    }
}
