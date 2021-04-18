package com.springboot.repository;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.springboot.model.Profissao;

@Transactional
@Repository
public interface ProfissaoRepository extends CrudRepository<Profissao, Long>{

}
