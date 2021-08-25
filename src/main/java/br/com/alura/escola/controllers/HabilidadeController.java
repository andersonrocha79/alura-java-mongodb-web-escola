package br.com.alura.escola.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import br.com.alura.escola.models.Aluno;
import br.com.alura.escola.models.Habilidade;
import br.com.alura.escola.repositories.AlunoRepository;

@Controller
public class HabilidadeController 
{
	
	@Autowired
	private AlunoRepository alunoRepository;
	
	@GetMapping("/habilidade/cadastrar/{id}")
	public String cadastrar(@PathVariable String id, Model model)
	{
		
		Aluno aluno = alunoRepository.obterAlunoPor(id);		
		model.addAttribute("aluno", aluno);		
		model.addAttribute("habilidade", new Habilidade());		
		return "habilidade/cadastrar";
		
	}
	
	@PostMapping("/habilidade/salvar/{id}")
	public String salvar(@PathVariable String id, @ModelAttribute Habilidade habilidade)
	{
		
		// busca o aluno a ser atualizado
		Aluno aluno = alunoRepository.obterAlunoPor(id);
		
		// adiciona a habilidade na lista de habilidades do aluno selecionado
		Aluno alunoAtualizado = aluno.adicionarHabilidade(aluno, habilidade);
		
		// salva o aluno 'atualizado' no banco de dados
		alunoRepository.salvar(alunoAtualizado);
				
		// redireciona para a tela de pesquisa de alunos
		return "redirect:/aluno/listar";
		
	}
	

}
