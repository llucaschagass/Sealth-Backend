package com.sealth.sealthapi.services;

import com.sealth.sealthapi.domain.vaccine.VaccineCatalog;
import com.sealth.sealthapi.domain.vaccine.VaccineDefinition;
import com.sealth.sealthapi.domain.vaccine.VaccineStatus;
import com.sealth.sealthapi.dto.VaccineStatusDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regra de situação das vacinas, com datas fixas. */
class VaccineServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

    private static VaccineDefinition vaccine(String code) {
        return VaccineCatalog.find(code).orElseThrow();
    }

    private static VaccineStatusDTO evaluate(String code, List<LocalDate> doses, Integer age) {
        return VaccineService.evaluate(vaccine(code), doses, age, TODAY);
    }

    @Test
    void withoutDosesAnApplicableVaccineIsPending() {
        VaccineStatusDTO status = evaluate("febre-amarela", List.of(), 30);

        assertEquals(VaccineStatus.PENDING, status.status());
        assertEquals(0, status.dosesTaken());
        assertNull(status.lastDoseAt());
        assertNull(status.nextDueDate());
    }

    @Test
    void withoutDosesAVaccineOutsideTheAgeRangeIsNotApplicable() {
        assertEquals(VaccineStatus.NOT_APPLICABLE, evaluate("triplice-viral", List.of(), 30).status());
        assertEquals(VaccineStatus.NOT_APPLICABLE, evaluate("hpv", List.of(), 30).status());
        assertEquals(VaccineStatus.NOT_APPLICABLE, evaluate("pneumococica-23", List.of(), 30).status());
        assertEquals(VaccineStatus.NOT_APPLICABLE, evaluate("hpv", List.of(), 8).status());
    }

    @Test
    void ageBoundariesAreInclusive() {
        assertEquals(VaccineStatus.PENDING, evaluate("hpv", List.of(), 9).status());
        assertEquals(VaccineStatus.PENDING, evaluate("hpv", List.of(), 14).status());
        assertEquals(VaccineStatus.NOT_APPLICABLE, evaluate("hpv", List.of(), 15).status());
        assertEquals(VaccineStatus.PENDING, evaluate("pneumococica-23", List.of(), 60).status());
        assertEquals(VaccineStatus.PENDING, evaluate("triplice-viral", List.of(), 29).status());
    }

    @Test
    void withoutKnownAgeEveryVaccineApplies() {
        for (VaccineDefinition vaccine : VaccineCatalog.ALL) {
            assertEquals(VaccineStatus.PENDING,
                    VaccineService.evaluate(vaccine, List.of(), null, TODAY).status(), vaccine.code());
        }
    }

    @Test
    void dosesTakenOutsideTheAgeRangeStillCount() {
        List<LocalDate> doses = List.of(TODAY.minusYears(20), TODAY.minusYears(19));

        VaccineStatusDTO status = evaluate("triplice-viral", doses, 40);

        assertEquals(VaccineStatus.UP_TO_DATE, status.status());
        assertEquals(2, status.dosesTaken());
    }

    @Test
    void incompleteSeriesIsPendingEvenWithSomeDoses() {
        VaccineStatusDTO status = evaluate("hepatite-b", List.of(TODAY.minusMonths(2), TODAY.minusMonths(1)), 30);

        assertEquals(VaccineStatus.PENDING, status.status());
        assertEquals(2, status.dosesTaken());
        assertEquals(TODAY.minusMonths(1), status.lastDoseAt());
    }

    @Test
    void completeSeriesWithoutBoosterIsUpToDate() {
        List<LocalDate> doses = List.of(TODAY.minusYears(10), TODAY.minusYears(10).plusMonths(1), TODAY.minusYears(9));

        VaccineStatusDTO status = evaluate("hepatite-b", doses, 30);

        assertEquals(VaccineStatus.UP_TO_DATE, status.status());
        assertNull(status.nextDueDate());
    }

    @Test
    void boosterIsDueOnTheExactDay() {
        LocalDate last = TODAY.minusMonths(12);

        VaccineStatusDTO status = evaluate("influenza", List.of(last), 30);

        assertEquals(VaccineStatus.DUE, status.status());
        assertEquals(TODAY, status.nextDueDate());
    }

    @Test
    void boosterIsDueAfterTheInterval() {
        VaccineStatusDTO status = evaluate("influenza", List.of(TODAY.minusYears(3)), 30);

        assertEquals(VaccineStatus.DUE, status.status());
        assertTrue(status.nextDueDate().isBefore(TODAY));
    }

    @Test
    void boosterIsSoonWithinThirtyDays() {
        // reforço em 30 dias: a janela começa exatamente hoje
        VaccineStatusDTO inThirty = evaluate("influenza", List.of(TODAY.minusMonths(12).plusDays(30)), 30);
        VaccineStatusDTO inThirtyOne = evaluate("influenza", List.of(TODAY.minusMonths(12).plusDays(31)), 30);

        assertEquals(VaccineStatus.SOON, inThirty.status());
        assertEquals(VaccineStatus.UP_TO_DATE, inThirtyOne.status());
    }

    @Test
    void boosterFarInTheFutureIsUpToDate() {
        VaccineStatusDTO status = evaluate("dt", List.of(TODAY.minusYears(1), TODAY.minusYears(2), TODAY.minusYears(3)), 30);

        assertEquals(VaccineStatus.UP_TO_DATE, status.status());
        assertEquals(TODAY.minusYears(1).plusMonths(120), status.nextDueDate());
    }

    @Test
    void lastDoseIsTheMostRecentRegardlessOfOrder() {
        List<LocalDate> doses = List.of(TODAY.minusYears(15), TODAY.minusYears(11), TODAY.minusYears(14));

        VaccineStatusDTO status = evaluate("dt", doses, 30);

        assertEquals(TODAY.minusYears(11), status.lastDoseAt());
        assertEquals(VaccineStatus.DUE, status.status());
    }

    @Test
    void onlyDueSoonAndPendingAreAlerts() {
        assertTrue(VaccineStatus.DUE.isAlert());
        assertTrue(VaccineStatus.PENDING.isAlert());
        assertTrue(VaccineStatus.SOON.isAlert());
        assertFalse(VaccineStatus.UP_TO_DATE.isAlert());
        assertFalse(VaccineStatus.NOT_APPLICABLE.isAlert());
    }

    @Test
    void statusesAreOrderedFromMostToLeastUrgent() {
        assertEquals(List.of(VaccineStatus.DUE, VaccineStatus.PENDING, VaccineStatus.SOON,
                VaccineStatus.UP_TO_DATE, VaccineStatus.NOT_APPLICABLE), List.of(VaccineStatus.values()));
    }

    @Test
    void catalogCodesAreUniqueAndFindable() {
        Set<String> codes = VaccineCatalog.ALL.stream().map(VaccineDefinition::code).collect(Collectors.toSet());

        assertEquals(VaccineCatalog.ALL.size(), codes.size());
        assertTrue(VaccineCatalog.find("influenza").isPresent());
        assertTrue(VaccineCatalog.find("inexistente").isEmpty());
    }
}
