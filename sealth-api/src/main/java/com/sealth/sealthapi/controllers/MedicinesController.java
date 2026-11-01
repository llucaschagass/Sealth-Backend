package com.sealth.sealthapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/medicines")
@Tag(name = "Medicamentos", description = "Bulas disponíveis")
public class MedicinesController {

    // Lista de todos os medicamentos disponíveis
    private final List<String> allMedicines = List.of("Paracetamol", "Omeprazol", "Amoxicilina", "Atorvastatina", "Metformina");

    @Operation(summary = "Lista os nomes dos medicamentos com bula em PDF")
    @GetMapping("/pdf")
    public List<String> getAllPDFs() {
        return allMedicines;
    }
}
