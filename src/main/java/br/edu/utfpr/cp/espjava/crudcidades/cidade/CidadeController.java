package br.edu.utfpr.cp.espjava.crudcidades.cidade;

import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class CidadeController {
    
    private Set<Cidade> cidades;

    private final CidadeRepository repository;

    public CidadeController(CidadeRepository repository) {
        cidades = new HashSet<>();
        this.repository = repository;
    }

    @GetMapping("/")
    public String listar(Model memoria, Principal usuario, HttpSession sessao, HttpServletResponse response) {

        response.addCookie(new Cookie("listar", LocalDateTime.now().toString()));

        memoria.addAttribute("listaCidades", repository
                                                .findAll()
                                                .stream()
                                                .map(cidade -> new Cidade(cidade.getNome(), cidade.getEstado()))
                                                .collect(Collectors.toList()));

        sessao.setAttribute("usuarioAtual", usuario.getName());

        return "/crud";
    }

    @PostMapping("/criar")
    public String criar(@Valid Cidade cidade, BindingResult validacao, Model memoria, HttpServletResponse response) {
        
        response.addCookie(new Cookie("criar", LocalDateTime.now().toString()));

        if (validacao.hasErrors()) {
            validacao
                .getFieldErrors()
                .forEach(error ->
                        memoria.addAttribute(
                            error.getField(),
                            error.getDefaultMessage())
                        );

            memoria.addAttribute("nomeInformado", cidade.getNome());
            memoria.addAttribute("estadoInformado", cidade.getEstado());
            memoria.addAttribute("listaCidades", cidades);

            return ("/crud");
        } else {
            repository.save(cidade.clonar());
        }

        return "redirect:/";
    }

    @GetMapping("/excluir")
    public String excluir(@RequestParam String nome, @RequestParam String estado, HttpServletResponse response) {
        
        response.addCookie(new Cookie("excluir", LocalDateTime.now().toString()));

        var cidadeEstadoEncontrada = repository.findByNomeAndEstado(nome, estado);

        cidadeEstadoEncontrada.ifPresent(repository::delete);

        return "redirect:/";
    }

    @GetMapping("/preparaAlterar")
    public String preparaAlterar(@RequestParam String nome, @RequestParam String estado, Model memoria) {
        Optional<CidadeEntidade> cidadeAtual = repository.findByNomeAndEstado(nome, estado);

        cidadeAtual.ifPresent(cidadeEncontrada -> {
            memoria.addAttribute("cidadeAtual", cidadeEncontrada);
            memoria.addAttribute("listaCidades", repository.findAll());
        });

        return "/crud";
    }

    @PostMapping("/alterar")
    public String alterar(@RequestParam String nomeAtual, @RequestParam String estadoAtual, Cidade cidade, HttpServletResponse response) {
        
        response.addCookie(new Cookie("alterar", LocalDateTime.now().toString()));

        Optional<CidadeEntidade> cidadeAtual = repository.findByNomeAndEstado(nomeAtual, estadoAtual);

        if (cidadeAtual.isPresent()) {
            
            CidadeEntidade cidadeEncontrada = cidadeAtual.get();
            cidadeEncontrada.setNome(cidade.getNome());
            cidadeEncontrada.setEstado(cidade.getEstado());

            repository.saveAndFlush(cidadeEncontrada);
        }

        return "redirect:/";
    }

    @GetMapping("/mostrar")
    @ResponseBody
    public String mostraCookieAlterar(@CookieValue String listar) {
        return "Último acesso ao método listar(): " + listar;
    }
}
