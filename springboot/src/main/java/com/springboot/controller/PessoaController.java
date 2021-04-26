package com.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.springboot.model.Pessoa;
import com.springboot.model.Telefone;
import com.springboot.repository.PessoaRepository;
import com.springboot.repository.ProfissaoRepository;
import com.springboot.repository.TelefoneRepository;
import com.springboot.service.ReportUtil;

@Controller
@RequestMapping("/*")
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ReportUtil reportUtil;
	
	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	@RequestMapping(method=RequestMethod.GET, value ="**/cadastropessoa")
	public ModelAndView inicio() {
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of
				(0, 5, Sort.by("nome"))));
		modelAndView.addObject("pessoaobj", new Pessoa());
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		return modelAndView;
	}
	
	@RequestMapping(method=RequestMethod.POST, 
			value="**/salvarpessoa", consumes = {"multipart/form-data"})
	public ModelAndView salvar(@Valid Pessoa pessoa, 
			BindingResult binding, final MultipartFile file) throws IOException {
		
		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));
		
		if(binding.hasErrors()) {
			ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
			andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of
					(0, 5, Sort.by("nome"))));
			andView.addObject("pessoaobj", pessoa);
			
			List<String> msg = new ArrayList<String>();
			for(ObjectError error : binding.getAllErrors()) {
				msg.add(error.getDefaultMessage()); // Vem das anotações @NotEmpty e outras
			}
			
			andView.addObject("msg", msg);
			andView.addObject("profissoes", profissaoRepository.findAll());
			return andView;
			
		}
		
		if(file.getSize() > 0) { // Cadastrando um currículo
			pessoa.setCurriculo(file.getBytes());
			pessoa.setTipoFileCurriculo(file.getContentType());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
		}else {
			if(pessoa.getId() != null && pessoa.getId() > 0) { // editando
				Pessoa pessoaTemp = pessoaRepository.findById(pessoa.getId()).get();
				
				pessoa.setCurriculo(pessoaTemp.getCurriculo());
				pessoa.setTipoFileCurriculo(pessoaTemp.getTipoFileCurriculo());
				pessoa.setNomeFileCurriculo(pessoaTemp.getNomeFileCurriculo());
			}
		}
		pessoaRepository.save(pessoa);
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of
				(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		
		return andView;
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/listapessoas")
	public ModelAndView pessoas() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of
				(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		return andView;
	}
	
	@GetMapping("**/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa){
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		modelAndView.addObject("pessoaobj", pessoa.get());
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		return modelAndView;
		
	}
	
	@GetMapping("**/removerpessoa/{idpessoa}")
	public ModelAndView remover(@PathVariable("idpessoa") Long idpessoa){
		pessoaRepository.deleteById(idpessoa);
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of
				(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		return andView; 
	}
	
	@PostMapping("**/consultarpessoa")
	public ModelAndView consultar(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("peqsexo") String peqsexo,
					@PageableDefault(size = 5, sort = {"nome"}) Pageable pageable) {
		
		Page<Pessoa> pessoas = null;
		
		if(peqsexo != null && !peqsexo.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexoPage(nomepesquisa, peqsexo, pageable);
		}else {
			pessoas = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		}
		ModelAndView andView = new ModelAndView("/cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoas);
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("nomepesquisa", nomepesquisa);
		return andView;
	}
	
	@GetMapping("**/telefones/{idpessoa}")
	public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa){
		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaobj", pessoa.get());
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		return modelAndView;
	}
	
	@PostMapping("**/addfonepessoa/{pessoaid}")
	public ModelAndView addFonePessoa(@Valid Telefone telefone, BindingResult result
			,@PathVariable("pessoaid") Long pessoaid) {
		
		if(result.hasErrors()) {
			ModelAndView andView = new ModelAndView("cadastro/telefones");
			Pessoa pessoa = pessoaRepository.findById(pessoaid).get();
			andView.addObject("pessoaobj", pessoa);
			andView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
			
			
			List<String> msg = new ArrayList<String>();
			for(ObjectError erro : result.getAllErrors()) {
				msg.add(erro.getDefaultMessage());
			}
			
			andView.addObject("msg", msg);
			return andView;
		}
		
		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();
		telefone.setPessoa(pessoa);
		telefoneRepository.save(telefone);
		
		ModelAndView andView = new ModelAndView("cadastro/telefones");
		andView.addObject("pessoaobj", pessoa);
		andView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
		
		
		return andView;
	}
	
	@GetMapping("**/consultarpessoa")
	public void imprimePdf(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("peqsexo") String peqsexo,
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception{
		
		List<Pessoa> pessoas = new ArrayList<Pessoa>();
		//Busca por nome e sexo
		if(peqsexo != null && !peqsexo.isEmpty()
				&& nomepesquisa != null && !nomepesquisa.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexo(nomepesquisa, peqsexo);
			// Busca somente por nome
		}else if(nomepesquisa != null && nomepesquisa.isEmpty()) {
			pessoas = pessoaRepository.findPessoaByName(nomepesquisa);
			// Busca por sexo
		}else if(peqsexo != null && nomepesquisa.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexoFM(peqsexo);}
		// Busca todos
		else {
			Iterable <Pessoa> iterator = pessoaRepository.findAll();
			for (Pessoa pessoa : iterator) {
				pessoas.add(pessoa);		
				}
		}
		// Chamar o serviço que faz a geração do relatório
		byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());
		
		// Tamanho da resposta
		response.setContentLength(pdf.length);
		
		// Definir a resposta o tipo de arquivo
		response.setContentType("application/octet-stream");
		
		// Definir o cabeçalho da resposta
		String headerKey = "Content-disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		response.setHeader(headerKey, headerValue);
		
		// Finaliza a resposta pro navegador
		response.getOutputStream().write(pdf);
	}
	
	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarCurriculo(@PathVariable("idpessoa") Long idpessoa,
			HttpServletResponse response) throws IOException {
		
		// Consultar objeto pessoa no banco de dados
		
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
			if(pessoa.getCurriculo() != null) {
				// Setar o tamanho da resposta
				response.setContentLength(pessoa.getCurriculo().length);
				// Tipo do arquivo para download ou pode ser generico application/octet-stream
				response.setContentType(pessoa.getTipoFileCurriculo());
				// Define o cabeçalho da resposta
				String headerKey = "content-Disposition";
				String headerValue = String.format("attachament; filename=\"%s\"", pessoa.getNomeFileCurriculo());
				response.setHeader(headerKey, headerValue);
				// Finaliza a resposta passando o arquivo
				response.getOutputStream().write(pessoa.getCurriculo());
			}
				
	}
	
	
	@GetMapping("**/editartelefone/{telefoneid}")
	public ModelAndView editarTelefone(@PathVariable("telefoneid") Long idpessoa ) {
		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaobj", pessoa.get());
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		return modelAndView;

	}
	
	
	@GetMapping("**/removertelefone/{telefoneid}")
	public ModelAndView deletarTelefone(@PathVariable("telefoneid") Long telefoneid){
		
		Pessoa pessoa = telefoneRepository.findById(telefoneid).get().getPessoa();
		
		telefoneRepository.deleteById(telefoneid);
		
		ModelAndView andView = new ModelAndView("/cadastro/telefones");
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
		return andView;
	}
		
	@GetMapping("/pessoaspag")
	public ModelAndView carregaPessoaPorPaginacao(@PageableDefault(size = 5)
	Pageable pageable, ModelAndView andView, @RequestParam("nomepesquisa") String nomepesquisa) {
		Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		andView.addObject("pessoas", pagePessoa);
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("nomepesquisa", nomepesquisa);
		andView.setViewName("cadastro/cadastropessoa");
		
		return andView;
	}
}
