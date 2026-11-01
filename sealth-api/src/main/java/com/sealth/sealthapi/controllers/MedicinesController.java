package com.sealth.sealthapi.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/medicines")
public class MedicinesController {

    // Lista de todos os medicamentos disponíveis
    private final List<String> allMedicines = List.of("Paracetamol", "Omeprazol", "Amoxicilina", "Atorvastatina", "Metformina");

    @GetMapping("/pdf")
    public List<String> getAllPDFs() {
        return allMedicines;
    }
}
