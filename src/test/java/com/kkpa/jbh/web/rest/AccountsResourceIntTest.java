package com.kkpa.jbh.web.rest;

import com.kkpa.jbh.JbhApp;

import com.kkpa.jbh.domain.Accounts;
import com.kkpa.jbh.repository.AccountsRepository;
import com.kkpa.jbh.service.AccountsService;
import com.kkpa.jbh.service.dto.AccountsDTO;
import com.kkpa.jbh.service.mapper.AccountsMapper;
import com.kkpa.jbh.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;


import static com.kkpa.jbh.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AccountsResource REST controller.
 *
 * @see AccountsResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JbhApp.class)
public class AccountsResourceIntTest {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_CREATED_AT = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREATED_AT = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_UPDATED_AT = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_UPDATED_AT = LocalDate.now(ZoneId.systemDefault());

    @Autowired
    private AccountsRepository accountsRepository;


    @Autowired
    private AccountsMapper accountsMapper;
    

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restAccountsMockMvc;

    private Accounts accounts;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AccountsResource accountsResource = new AccountsResource(accountsService);
        this.restAccountsMockMvc = MockMvcBuilders.standaloneSetup(accountsResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Accounts createEntity(EntityManager em) {
        Accounts accounts = new Accounts()
            .description(DEFAULT_DESCRIPTION)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
        return accounts;
    }

    @Before
    public void initTest() {
        accounts = createEntity(em);
    }

    @Test
    @Transactional
    public void createAccounts() throws Exception {
        int databaseSizeBeforeCreate = accountsRepository.findAll().size();

        // Create the Accounts
        AccountsDTO accountsDTO = accountsMapper.toDto(accounts);
        restAccountsMockMvc.perform(post("/api/accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(accountsDTO)))
            .andExpect(status().isCreated());

        // Validate the Accounts in the database
        List<Accounts> accountsList = accountsRepository.findAll();
        assertThat(accountsList).hasSize(databaseSizeBeforeCreate + 1);
        Accounts testAccounts = accountsList.get(accountsList.size() - 1);
        assertThat(testAccounts.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAccounts.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testAccounts.getUpdatedAt()).isEqualTo(DEFAULT_UPDATED_AT);
    }

    @Test
    @Transactional
    public void createAccountsWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = accountsRepository.findAll().size();

        // Create the Accounts with an existing ID
        accounts.setId(1L);
        AccountsDTO accountsDTO = accountsMapper.toDto(accounts);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAccountsMockMvc.perform(post("/api/accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(accountsDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Accounts in the database
        List<Accounts> accountsList = accountsRepository.findAll();
        assertThat(accountsList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllAccounts() throws Exception {
        // Initialize the database
        accountsRepository.saveAndFlush(accounts);

        // Get all the accountsList
        restAccountsMockMvc.perform(get("/api/accounts?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(accounts.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }
    

    @Test
    @Transactional
    public void getAccounts() throws Exception {
        // Initialize the database
        accountsRepository.saveAndFlush(accounts);

        // Get the accounts
        restAccountsMockMvc.perform(get("/api/accounts/{id}", accounts.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(accounts.getId().intValue()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }
    @Test
    @Transactional
    public void getNonExistingAccounts() throws Exception {
        // Get the accounts
        restAccountsMockMvc.perform(get("/api/accounts/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAccounts() throws Exception {
        // Initialize the database
        accountsRepository.saveAndFlush(accounts);

        int databaseSizeBeforeUpdate = accountsRepository.findAll().size();

        // Update the accounts
        Accounts updatedAccounts = accountsRepository.findById(accounts.getId()).get();
        // Disconnect from session so that the updates on updatedAccounts are not directly saved in db
        em.detach(updatedAccounts);
        updatedAccounts
            .description(UPDATED_DESCRIPTION)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        AccountsDTO accountsDTO = accountsMapper.toDto(updatedAccounts);

        restAccountsMockMvc.perform(put("/api/accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(accountsDTO)))
            .andExpect(status().isOk());

        // Validate the Accounts in the database
        List<Accounts> accountsList = accountsRepository.findAll();
        assertThat(accountsList).hasSize(databaseSizeBeforeUpdate);
        Accounts testAccounts = accountsList.get(accountsList.size() - 1);
        assertThat(testAccounts.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAccounts.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testAccounts.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    public void updateNonExistingAccounts() throws Exception {
        int databaseSizeBeforeUpdate = accountsRepository.findAll().size();

        // Create the Accounts
        AccountsDTO accountsDTO = accountsMapper.toDto(accounts);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restAccountsMockMvc.perform(put("/api/accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(accountsDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Accounts in the database
        List<Accounts> accountsList = accountsRepository.findAll();
        assertThat(accountsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAccounts() throws Exception {
        // Initialize the database
        accountsRepository.saveAndFlush(accounts);

        int databaseSizeBeforeDelete = accountsRepository.findAll().size();

        // Get the accounts
        restAccountsMockMvc.perform(delete("/api/accounts/{id}", accounts.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Accounts> accountsList = accountsRepository.findAll();
        assertThat(accountsList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Accounts.class);
        Accounts accounts1 = new Accounts();
        accounts1.setId(1L);
        Accounts accounts2 = new Accounts();
        accounts2.setId(accounts1.getId());
        assertThat(accounts1).isEqualTo(accounts2);
        accounts2.setId(2L);
        assertThat(accounts1).isNotEqualTo(accounts2);
        accounts1.setId(null);
        assertThat(accounts1).isNotEqualTo(accounts2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AccountsDTO.class);
        AccountsDTO accountsDTO1 = new AccountsDTO();
        accountsDTO1.setId(1L);
        AccountsDTO accountsDTO2 = new AccountsDTO();
        assertThat(accountsDTO1).isNotEqualTo(accountsDTO2);
        accountsDTO2.setId(accountsDTO1.getId());
        assertThat(accountsDTO1).isEqualTo(accountsDTO2);
        accountsDTO2.setId(2L);
        assertThat(accountsDTO1).isNotEqualTo(accountsDTO2);
        accountsDTO1.setId(null);
        assertThat(accountsDTO1).isNotEqualTo(accountsDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(accountsMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(accountsMapper.fromId(null)).isNull();
    }
}
