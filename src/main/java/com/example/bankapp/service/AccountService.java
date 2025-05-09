package com.example.bankapp.service;

import com.example.bankapp.email.EmailSenderService;
import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private EmailSenderService emailsenderservice;

    public Account findAccountByUsername(String username) {
        return accountRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public Account registerAccount(String username, String email, String password) {
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Account account = new Account();
        account.setUsername(username);
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(password)); // Encrypt password
        account.setBalance(BigDecimal.ZERO); // Initial balance set to 0
        return accountRepository.save(account);
    }

    public void deposit(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction(
                amount,
                "Deposit",
                LocalDateTime.now(),
                account
        );
        transactionRepository.save(transaction);

        // Send Email if email exists
        if (account.getEmail() != null && !account.getEmail().isEmpty()) {
            String subject = "Deposit Successful";
            String body = "Dear " + account.getUsername() + ",\n\n" +
                          "You have deposited $" + amount + ".\n" +
                          "Current Balance: $" + account.getBalance() + "\n\n" +
                          "Thank you for banking with us.";
            emailsenderservice.sendSimpleEmail(account.getEmail(), subject, body);
        }
    }

    public void withdraw(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction(
                amount,
                "Withdrawal",
                LocalDateTime.now(),
                account
        );
        transactionRepository.save(transaction);

        // Send Email if email exists
        if (account.getEmail() != null && !account.getEmail().isEmpty()) {
            String subject = "Withdrawal Successful";
            String body = "Dear " + account.getUsername() + ",\n\n" +
                          "You have withdrawn $" + amount + ".\n" +
                          "Current Balance: $" + account.getBalance() + "\n\n" +
                          "Thank you for banking with us.";
            emailsenderservice.sendSimpleEmail(account.getEmail(), subject, body);
        }
    }

    public List<Transaction> getTransactionHistory(Account account) {
        return transactionRepository.findByAccountId(account.getId());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Account account = findAccountByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException("Username or Password not found");
        }
        return new Account(
                account.getUsername(),
                account.getPassword(),
                account.getBalance(),
                account.getTransactions(),
                authorities());
    }

    public Collection<? extends GrantedAuthority> authorities() {
        return Arrays.asList(new SimpleGrantedAuthority("USER"));
    }

    public void transferAmount(Account fromAccount, String toUsername, BigDecimal amount) {
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        Account toAccount = accountRepository.findByUsername(toUsername)
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        // Deduct from sender's account
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        // Add to recipient's account
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        // Create transaction records for both accounts
        Transaction debitTransaction = new Transaction(
                amount,
                "Transfer Out to " + toAccount.getUsername(),
                LocalDateTime.now(),
                fromAccount
        );
        transactionRepository.save(debitTransaction);

        Transaction creditTransaction = new Transaction(
                amount,
                "Transfer In from " + fromAccount.getUsername(),
                LocalDateTime.now(),
                toAccount
        );
        transactionRepository.save(creditTransaction);

        // Send Email to sender if email exists
        if (fromAccount.getEmail() != null && !fromAccount.getEmail().isEmpty()) {
            String senderSubject = "Transfer Successful";
            String senderBody = "Dear " + fromAccount.getUsername() + ",\n\n" +
                               "You have transferred $" + amount + " to " + toAccount.getUsername() + ".\n" +
                               "Current Balance: $" + fromAccount.getBalance() + "\n\n" +
                               "Thank you for banking with us.";
            emailsenderservice.sendSimpleEmail(fromAccount.getEmail(), senderSubject, senderBody);
        }

        // Send Email to recipient if email exists
        if (toAccount.getEmail() != null && !toAccount.getEmail().isEmpty()) {
            String recipientSubject = "Transfer Received";
            String recipientBody = "Dear " + toAccount.getUsername() + ",\n\n" +
                                  "You have received $" + amount + " from " + fromAccount.getUsername() + ".\n" +
                                  "Current Balance: $" + toAccount.getBalance() + "\n\n" +
                                  "Thank you for banking with us.";
            emailsenderservice.sendSimpleEmail(toAccount.getEmail(), recipientSubject, recipientBody);
        }
    }
}