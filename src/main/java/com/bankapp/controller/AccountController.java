package com.bankapp.controller;

import com.bankapp.model.Account;
import com.bankapp.repository.AccountRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository repo;

    public AccountController(AccountRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Account> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Account create(@RequestBody Account account) {
        return repo.save(account);
    }

    @GetMapping("/{id}")
    public Account getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
