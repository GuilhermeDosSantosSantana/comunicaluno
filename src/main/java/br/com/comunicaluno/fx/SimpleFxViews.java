package br.com.comunicaluno.fx;

import br.com.comunicaluno.model.Disciplina;
import br.com.comunicaluno.model.Evento;
import br.com.comunicaluno.model.Notificacao;
import br.com.comunicaluno.model.Post;
import br.com.comunicaluno.model.Curso;
import br.com.comunicaluno.model.Usuario;
import br.com.comunicaluno.service.ArquivoService;
import br.com.comunicaluno.service.ChamadoService;
import br.com.comunicaluno.service.CursoService;
import br.com.comunicaluno.service.DisciplinaService;
import br.com.comunicaluno.service.EventoService;
import br.com.comunicaluno.service.NotificacaoService;
import br.com.comunicaluno.service.PostService;
import br.com.comunicaluno.service.UsuarioService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class SimpleFxViews {

    private static final Set<String> EQUIPE = Set.of("ADMIN", "COORDENADOR", "PROF");

    private SimpleFxViews() {
    }

    public static Node avisosNotificacoes(Usuario usuario, Supplier<String> busca, Runnable atualizar) {
        VBox root = telaBase();
        root.getChildren().add(FxTheme.title("Avisos e Notificações", 22));
        root.getChildren().add(FxTheme.muted("Acesse os avisos publicados e as notificações da sua conta na mesma tela."));

        Button avisosButton = FxTheme.primaryButton("Avisos");
        Button notificacoesButton = FxTheme.secondaryButton("Notificações");
        HBox botoes = new HBox(8, avisosButton, notificacoesButton);
        botoes.setAlignment(Pos.CENTER_LEFT);

        VBox conteudo = new VBox(12);
        VBox.setVgrow(conteudo, Priority.ALWAYS);

        Runnable abrirAvisos = () -> {
            selecionarAba(avisosButton, notificacoesButton);
            trocarConteudo(conteudo, avisos(usuario, busca, atualizar));
        };
        Runnable abrirNotificacoes = () -> {
            selecionarAba(notificacoesButton, avisosButton);
            trocarConteudo(conteudo, notificacoes(usuario, atualizar));
        };

        avisosButton.setOnAction(e -> abrirAvisos.run());
        notificacoesButton.setOnAction(e -> abrirNotificacoes.run());

        root.getChildren().addAll(botoes, conteudo);
        abrirAvisos.run();
        return root;
    }

    public static Node cursosDisciplinas(Usuario usuario, Runnable atualizar) {
        VBox root = telaBase();
        root.getChildren().add(FxTheme.title("Cursos e Disciplinas", 22));
        root.getChildren().add(FxTheme.muted("Gerencie ou consulte cursos e disciplinas usando os botões abaixo."));

        Button cursosButton = FxTheme.primaryButton("Cursos");
        Button disciplinasButton = FxTheme.secondaryButton("Disciplinas");
        HBox botoes = new HBox(8, cursosButton, disciplinasButton);
        botoes.setAlignment(Pos.CENTER_LEFT);

        VBox conteudo = new VBox(12);
        VBox.setVgrow(conteudo, Priority.ALWAYS);

        Runnable abrirCursos = () -> {
            selecionarAba(cursosButton, disciplinasButton);
            trocarConteudo(conteudo, cursos(usuario, atualizar));
        };
        Runnable abrirDisciplinas = () -> {
            selecionarAba(disciplinasButton, cursosButton);
            trocarConteudo(conteudo, disciplinas(usuario));
        };

        cursosButton.setOnAction(e -> abrirCursos.run());
        disciplinasButton.setOnAction(e -> abrirDisciplinas.run());

        root.getChildren().addAll(botoes, conteudo);
        abrirCursos.run();
        return root;
    }

    public static Node perfilConfiguracoes(Usuario usuario, Runnable atualizar) {
        VBox root = telaBase();
        root.getChildren().add(FxTheme.title("Perfil e Configurações", 22));
        root.getChildren().add(FxTheme.muted("Atualize seus dados pessoais e ajuste preferências básicas da interface na mesma área."));

        Button perfilButton = FxTheme.primaryButton("Perfil");
        Button configuracoesButton = FxTheme.secondaryButton("Configurações");
        HBox botoes = new HBox(8, perfilButton, configuracoesButton);
        botoes.setAlignment(Pos.CENTER_LEFT);

        VBox conteudo = new VBox(12);
        VBox.setVgrow(conteudo, Priority.ALWAYS);

        Runnable abrirPerfil = () -> {
            selecionarAba(perfilButton, configuracoesButton);
            trocarConteudo(conteudo, perfil(usuario, atualizar));
        };
        Runnable abrirConfiguracoes = () -> {
            selecionarAba(configuracoesButton, perfilButton);
            trocarConteudo(conteudo, configuracoes(usuario));
        };

        perfilButton.setOnAction(e -> abrirPerfil.run());
        configuracoesButton.setOnAction(e -> abrirConfiguracoes.run());

        root.getChildren().addAll(botoes, conteudo);
        abrirPerfil.run();
        return root;
    }

    public static Node avisos(Usuario usuario, Supplier<String> busca, Runnable atualizar) {
        VBox root = telaBase();
        PostService service = new PostService();

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label titulo = FxTheme.title("Avisos", 22);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button atualizarLista = FxTheme.secondaryButton("Atualizar lista");
        header.getChildren().addAll(titulo, spacer, atualizarLista);

        VBox lista = new VBox(10);

        Runnable[] carregar = new Runnable[1];
        carregar[0] = () -> {
            lista.getChildren().clear();
            try {
                List<Post> avisos = service.listarFeed(usuario, "MEU_PUBLICO", busca.get()).stream()
                        .filter(post -> "AVISO".equals(post.getTipoPost()) || "EVENTO".equals(post.getTipoPost()))
                        .toList();
                if (avisos.isEmpty()) {
                    lista.getChildren().add(empty("Nenhum aviso encontrado.", FxAssets.NOTIFICATION));
                }
                for (Post post : avisos) {
                    VBox card = FxTheme.card(8);
                    HBox topo = new HBox(10);
                    topo.setAlignment(Pos.CENTER_LEFT);
                    Region topoSpacer = new Region();
                    HBox.setHgrow(topoSpacer, Priority.ALWAYS);
                    topo.getChildren().addAll(FxTheme.title(valor(post.getTitulo(), "Aviso"), 17), topoSpacer);
                    if (service.podeArquivar(post, usuario)) {
                        Button excluir = FxTheme.secondaryButton("Excluir");
                        excluir.setStyle(excluir.getStyle() + "-fx-text-fill: " + FxTheme.ALERTA + ";");
                        excluir.setOnAction(e -> {
                            if (FxDialogs.confirmar("Excluir este aviso? Ele será arquivado e sairá das listagens.")) {
                                try {
                                    service.arquivarPost(post.getIdPost(), usuario, "Excluído pela interface.");
                                    carregar[0].run();
                                    atualizar.run();
                                } catch (Exception ex) {
                                    FxDialogs.erro(ex.getMessage());
                                }
                            }
                        });
                        topo.getChildren().add(excluir);
                    }
                    card.getChildren().addAll(topo, FxTheme.muted(post.getPublicoAlvo() + " - " + data(post.getCreatedAt())),
                            texto(post.getTexto()));
                    lista.getChildren().add(card);
                }
            } catch (Exception ex) {
                lista.getChildren().add(FxTheme.muted("Avisos indisponíveis: " + ex.getMessage()));
            }
        };

        atualizarLista.setOnAction(e -> {
            carregar[0].run();
            atualizar.run();
        });

        root.getChildren().addAll(header, lista);
        carregar[0].run();
        return scroll(root);
    }

    public static Node notificacoes(Usuario usuario, Runnable atualizar) {
        VBox root = telaBase();
        NotificacaoService service = new NotificacaoService();

        Label titulo = FxTheme.title("Notificações", 22);
        Label resumo = FxTheme.muted("");

        Button marcarLidas = FxTheme.primaryButton("Marcar todas como lidas");
        Button atualizarLista = FxTheme.secondaryButton("Atualizar lista");
        HBox acoes = new HBox(8, marcarLidas, atualizarLista);
        acoes.setAlignment(Pos.CENTER_LEFT);

        VBox lista = new VBox(10);

        Runnable carregar = () -> {
            try {
                lista.getChildren().clear();
                int naoLidas = service.contarNaoLidas(usuario);
                if (naoLidas == 0) {
                    resumo.setText("Nenhuma notificação não lida no momento.");
                } else if (naoLidas == 1) {
                    resumo.setText("1 notificação não lida.");
                } else {
                    resumo.setText(naoLidas + " notificações não lidas.");
                }

                List<Notificacao> notificacoes = service.listarRecentes(usuario, 50);
                if (notificacoes.isEmpty()) {
                    lista.getChildren().add(empty("Nenhuma notificação encontrada.", FxAssets.NOTIFICATION));
                    return;
                }

                for (Notificacao notificacao : notificacoes) {
                    lista.getChildren().add(cardNotificacao(notificacao));
                }
            } catch (Exception ex) {
                lista.getChildren().clear();
                lista.getChildren().add(FxTheme.muted("Notificações indisponíveis: " + ex.getMessage()));
            }
        };

        atualizarLista.setOnAction(e -> carregar.run());
        marcarLidas.setOnAction(e -> {
            try {
                service.marcarTodasComoLidas(usuario);
                FxDialogs.info("Notificações marcadas como lidas.");
                carregar.run();
                atualizar.run();
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        });

        root.getChildren().addAll(titulo, resumo, acoes, lista);
        carregar.run();
        return scroll(root);
    }

    public static Node eventos(Usuario usuario, Runnable atualizar) {
        VBox root = telaBase();
        EventoService eventoService = new EventoService();
        ArquivoService arquivoService = new ArquivoService();
        root.getChildren().add(FxTheme.title("Eventos", 22));

        if (EQUIPE.contains(usuario.getTipoPerfil())) {
            VBox form = FxTheme.card(8);
            TextField titulo = campo("Título");
            TextArea descricao = area("Descrição");
            TextField local = campo("Local");
            DatePicker data = new DatePicker();
            data.setPromptText("Data");
            data.setValue(LocalDateTime.now().plusDays(1).toLocalDate());
            data.setPrefHeight(38);
            data.setStyle(FxTheme.inputStyle());
            ComboBox<String> hora = comboNumerico(0, 23, 1, "19");
            ComboBox<String> minuto = comboNumerico(0, 55, 5, "00");
            HBox dataHora = new HBox(8, data, hora, minuto);
            ComboBox<String> publico = new ComboBox<>();
            publico.getItems().addAll("TODOS", "ALUNOS", "PROFESSORES", "COORDENADORES");
            publico.setValue("TODOS");
            publico.setStyle(FxTheme.inputStyle());
            final String[] imagem = {null};
            Label imagemLabel = FxTheme.muted("Nenhuma capa selecionada.");
            Button selecionar = FxTheme.secondaryButton("Selecionar capa");
            selecionar.setOnAction(e -> {
                File file = new FileChooser().showOpenDialog(root.getScene().getWindow());
                if (file != null) {
                    imagem[0] = arquivoService.salvarUpload(file, "eventos");
                    imagemLabel.setText(file.getName());
                }
            });
            Button salvar = FxTheme.primaryButton("Criar evento");
            salvar.setOnAction(e -> {
                try {
                    Evento evento = new Evento();
                    evento.setTitulo(titulo.getText());
                    evento.setDescricao(descricao.getText());
                    evento.setLocalEvento(local.getText());
                    if (data.getValue() == null) {
                        throw new IllegalArgumentException("Selecione a data do evento.");
                    }
                    evento.setDataHora(LocalDateTime.of(data.getValue(),
                            LocalTime.of(Integer.parseInt(hora.getValue()), Integer.parseInt(minuto.getValue()))));
                    evento.setPublicoAlvo(publico.getValue());
                    evento.setImagemPath(imagem[0]);
                    eventoService.criarEvento(evento, usuario);
                    FxDialogs.info("Evento criado.");
                    atualizar.run();
                } catch (Exception ex) {
                    FxDialogs.erro(ex.getMessage());
                }
            });
            form.getChildren().addAll(FxTheme.title("Novo evento", 16), titulo, descricao, local, dataHora,
                    publico, selecionar, imagemLabel, salvar);
            root.getChildren().add(form);
        }

        try {
            List<Evento> eventos = eventoService.listarProximos(usuario, 20);
            for (Evento evento : eventos) {
                VBox card = FxTheme.card(8);
                HBox topo = new HBox(10);
                topo.setAlignment(Pos.CENTER_LEFT);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                topo.getChildren().addAll(FxTheme.title(evento.getTitulo(), 18), spacer);
                if (eventoService.podeArquivar(evento, usuario)) {
                    Button excluir = FxTheme.secondaryButton("Excluir");
                    excluir.setStyle(excluir.getStyle() + "-fx-text-fill: " + FxTheme.ALERTA + ";");
                    excluir.setOnAction(e -> {
                        if (FxDialogs.confirmar("Excluir este evento? Ele será arquivado e sairá das listagens.")) {
                            try {
                                eventoService.arquivarEvento(evento.getIdEvento(), usuario, "Excluído pela interface.");
                                atualizar.run();
                            } catch (Exception ex) {
                                FxDialogs.erro(ex.getMessage());
                            }
                        }
                    });
                    topo.getChildren().add(excluir);
                }
                card.getChildren().addAll(
                        FxAssets.view(FxAssets.imageFromPathOrAsset(evento.getImagemPath(), FxAssets.EVENT_COVER), 520, 180),
                        topo,
                        FxTheme.muted(data(evento.getDataHora()) + " - " + valor(evento.getLocalEvento(), "Local a definir")),
                        texto(evento.getDescricao())
                );
                root.getChildren().add(card);
            }
        } catch (Exception ex) {
            root.getChildren().add(FxTheme.muted("Eventos indisponíveis: " + ex.getMessage()));
        }
        return scroll(root);
    }

    public static Node cursos(Usuario usuario, Runnable atualizar) {
        VBox root = telaBase();
        CursoService service = new CursoService();
        boolean podeGerir = service.podeGerirCursos(usuario);

        Label titulo = FxTheme.title("Cursos", 22);
        Label instrucao = FxTheme.muted(podeGerir
                ? "Cadastre, edite, ative ou inative os cursos disponíveis no sistema."
                : "Consulte os cursos ativos disponíveis no ComunicAluno.");
        Label resumo = FxTheme.muted("");

        ListView<Curso> lista = new ListView<>();
        lista.setPrefHeight(390);
        lista.setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-control-inner-background: "
                + FxTheme.CARD + "; -fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 8;");
        lista.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Curso item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String status = item.isAtivo() ? "ATIVO" : "INATIVO";
                    setText(item.getNome() + "  •  " + item.getCodigo() + "  •  " + status);
                }
                setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-text-fill: " + FxTheme.TEXTO + "; "
                        + "-fx-padding: 12; -fx-border-color: transparent transparent " + FxTheme.BORDA + " transparent;");
            }
        });

        Button atualizarLista = FxTheme.secondaryButton("Atualizar lista");
        final Runnable[] carregar = new Runnable[1];

        root.getChildren().addAll(titulo, instrucao, resumo);

        if (podeGerir) {
            VBox form = FxTheme.card(8);
            form.setPrefWidth(360);
            Label formTitulo = FxTheme.title("Cadastro do curso", 17);
            Label formHint = FxTheme.muted("Selecione um curso para editar ou preencha os campos para criar um novo.");
            TextField nome = campo("Nome do curso");
            TextField codigo = campo("Código do curso");
            CheckBox ativo = new CheckBox("Curso ativo");
            ativo.setSelected(true);
            ativo.setStyle("-fx-text-fill: " + FxTheme.TEXTO + "; -fx-font-weight: 700;");
            final Integer[] idCursoSelecionado = {null};

            Button novo = FxTheme.secondaryButton("Novo curso");
            Button salvar = FxTheme.primaryButton("Salvar curso");
            Button inativar = FxTheme.secondaryButton("Inativar selecionado");
            Button reativar = FxTheme.secondaryButton("Reativar selecionado");
            inativar.setStyle(inativar.getStyle() + "-fx-text-fill: " + FxTheme.ALERTA + ";");
            inativar.setDisable(true);
            reativar.setDisable(true);

            Runnable limparFormulario = () -> {
                idCursoSelecionado[0] = null;
                nome.clear();
                codigo.clear();
                ativo.setSelected(true);
                inativar.setDisable(true);
                reativar.setDisable(true);
                formHint.setText("Selecione um curso para editar ou preencha os campos para criar um novo.");
            };

            lista.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
                if (selecionado == null) {
                    limparFormulario.run();
                    return;
                }
                idCursoSelecionado[0] = selecionado.getIdCurso();
                nome.setText(valor(selecionado.getNome(), ""));
                codigo.setText(valor(selecionado.getCodigo(), ""));
                ativo.setSelected(selecionado.isAtivo());
                inativar.setDisable(!selecionado.isAtivo());
                reativar.setDisable(selecionado.isAtivo());
                formHint.setText("Editando: " + selecionado.getNome());
            });

            novo.setOnAction(e -> {
                lista.getSelectionModel().clearSelection();
                limparFormulario.run();
            });

            salvar.setOnAction(e -> {
                try {
                    Curso curso = new Curso();
                    curso.setIdCurso(idCursoSelecionado[0]);
                    curso.setNome(nome.getText());
                    curso.setCodigo(codigo.getText());
                    curso.setAtivo(ativo.isSelected());
                    service.salvarCurso(curso, usuario);
                    FxDialogs.info("Curso salvo com sucesso.");
                    if (carregar[0] != null) {
                        carregar[0].run();
                    }
                    atualizar.run();
                } catch (Exception ex) {
                    FxDialogs.erro(ex.getMessage());
                }
            });

            inativar.setOnAction(e -> {
                Curso selecionado = lista.getSelectionModel().getSelectedItem();
                if (selecionado == null) {
                    FxDialogs.info("Selecione um curso antes de inativar.");
                    return;
                }
                if (!FxDialogs.confirmar("Inativar o curso " + selecionado.getNome() + "?")) {
                    return;
                }
                try {
                    service.alterarStatusCurso(selecionado.getIdCurso(), false, usuario);
                    FxDialogs.info("Curso inativado com sucesso.");
                    if (carregar[0] != null) {
                        carregar[0].run();
                    }
                } catch (Exception ex) {
                    FxDialogs.erro(ex.getMessage());
                }
            });

            reativar.setOnAction(e -> {
                Curso selecionado = lista.getSelectionModel().getSelectedItem();
                if (selecionado == null) {
                    FxDialogs.info("Selecione um curso antes de reativar.");
                    return;
                }
                try {
                    service.alterarStatusCurso(selecionado.getIdCurso(), true, usuario);
                    FxDialogs.info("Curso reativado com sucesso.");
                    if (carregar[0] != null) {
                        carregar[0].run();
                    }
                } catch (Exception ex) {
                    FxDialogs.erro(ex.getMessage());
                }
            });

            HBox acoesForm = new HBox(8, salvar, novo);
            acoesForm.setAlignment(Pos.CENTER_LEFT);
            HBox acoesStatus = new HBox(8, inativar, reativar);
            acoesStatus.setAlignment(Pos.CENTER_LEFT);
            form.getChildren().addAll(formTitulo, formHint, nome, codigo, ativo, acoesForm, acoesStatus);

            VBox listaBox = FxTheme.card(8);
            HBox listaTopo = new HBox(8);
            listaTopo.setAlignment(Pos.CENTER_LEFT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            listaTopo.getChildren().addAll(FxTheme.title("Cursos cadastrados", 17), spacer, atualizarLista);
            VBox.setVgrow(lista, Priority.ALWAYS);
            listaBox.getChildren().addAll(listaTopo, lista);

            HBox conteudo = new HBox(14, listaBox, form);
            HBox.setHgrow(listaBox, Priority.ALWAYS);
            root.getChildren().add(conteudo);
        } else {
            HBox listaTopo = new HBox(8);
            listaTopo.setAlignment(Pos.CENTER_LEFT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            listaTopo.getChildren().addAll(FxTheme.title("Cursos disponíveis", 17), spacer, atualizarLista);
            root.getChildren().addAll(listaTopo, lista);
        }

        carregar[0] = () -> {
            try {
                List<Curso> cursos = podeGerir ? service.listarTodosCursos(usuario) : service.listarCursos(usuario);
                lista.getItems().setAll(cursos);
                lista.getSelectionModel().clearSelection();
                if (cursos.isEmpty()) {
                    resumo.setText("Nenhum curso encontrado.");
                } else if (cursos.size() == 1) {
                    resumo.setText("1 curso encontrado.");
                } else {
                    resumo.setText(cursos.size() + " cursos encontrados.");
                }
            } catch (Exception ex) {
                lista.getItems().clear();
                resumo.setText("Cursos indisponíveis: " + ex.getMessage());
            }
        };

        atualizarLista.setOnAction(e -> carregar[0].run());
        carregar[0].run();
        return scroll(root);
    }

    public static Node disciplinas(Usuario usuario) {
        VBox root = telaBase();
        DisciplinaService service = new DisciplinaService();
        root.getChildren().add(FxTheme.title("Disciplinas", 22));

        if (EQUIPE.contains(usuario.getTipoPerfil())) {
            VBox form = FxTheme.card(8);
            TextField nome = campo("Nome da disciplina");
            TextField codigo = campo("Código");
            TextField professor = campo("Professor responsável");
            Button salvar = FxTheme.primaryButton("Salvar disciplina");
            salvar.setOnAction(e -> {
                try {
                    Disciplina disciplina = new Disciplina();
                    disciplina.setNome(nome.getText());
                    disciplina.setCodigo(codigo.getText());
                    disciplina.setProfessorNome(professor.getText());
                    service.salvarDisciplina(disciplina, usuario);
                    FxDialogs.info("Disciplina salva.");
                } catch (Exception ex) {
                    FxDialogs.erro(ex.getMessage());
                }
            });
            form.getChildren().addAll(FxTheme.title("Nova disciplina", 16), nome, codigo, professor, salvar);
            root.getChildren().add(form);
        }

        FlowPane grid = new FlowPane(14, 14);
        try {
            for (Disciplina disciplina : service.listarParaUsuario(usuario)) {
                VBox card = FxTheme.card(8);
                card.setPrefWidth(260);
                card.getChildren().addAll(FxAssets.view(FxAssets.imageFromPathOrAsset(disciplina.getCapaPath(), FxAssets.DISCIPLINE_COVER), 220, 115),
                        FxTheme.title(disciplina.getNome(), 16),
                        FxTheme.muted(disciplina.getCodigo() + " - " + valor(disciplina.getProfessorNome(), "Professor a definir")),
                        FxTheme.muted("Progresso: " + disciplina.getProgressoPercentual() + "%"));
                grid.getChildren().add(card);
            }
        } catch (Exception ex) {
            grid.getChildren().add(FxTheme.muted("Disciplinas indisponíveis."));
        }
        root.getChildren().add(grid);
        return scroll(root);
    }

    public static Node perfil(Usuario usuario, Runnable atualizar) {
        VBox root = telaBase();
        UsuarioService usuarioService = new UsuarioService();
        CursoService cursoService = new CursoService();
        ArquivoService arquivoService = new ArquivoService();
        root.getChildren().add(FxTheme.title("Meu perfil", 22));
        VBox card = FxTheme.card(10);
        TextField nome = campo("Nome");
        nome.setText(usuario.getNome());
        ComboBox<String> curso = new ComboBox<>();
        curso.setEditable(true);
        curso.setPromptText("Curso");
        curso.setPrefHeight(38);
        curso.setStyle(FxTheme.inputStyle());
        try {
            for (Curso item : cursoService.listarCursos(usuario)) {
                curso.getItems().add(item.getNome());
            }
        } catch (Exception ignored) {
        }
        if (usuario.getCurso() != null && !usuario.getCurso().isBlank()) {
            if (!curso.getItems().contains(usuario.getCurso())) {
                curso.getItems().add(usuario.getCurso());
            }
            curso.setValue(usuario.getCurso());
        }
        TextField turma = campo("Turma");
        turma.setText(usuario.getTurma());
        Label email = FxTheme.muted(usuario.getEmail() + " - " + usuario.getTipoPerfil() + " - " + usuario.getStatusConta());
        final String[] avatar = {usuario.getAvatarPath()};
        Button avatarButton = FxTheme.secondaryButton("Alterar avatar");
        avatarButton.setOnAction(e -> {
            File file = new FileChooser().showOpenDialog(root.getScene().getWindow());
            if (file != null) {
                avatar[0] = arquivoService.salvarUpload(file, "avatares");
            }
        });
        Button salvar = FxTheme.primaryButton("Salvar perfil");
        salvar.setOnAction(e -> {
            try {
                Usuario atualizado = new Usuario();
                atualizado.setIdUsuario(usuario.getIdUsuario());
                atualizado.setNome(nome.getText());
                atualizado.setCurso(curso.getEditor().getText());
                atualizado.setTurma(turma.getText());
                atualizado.setAvatarPath(avatar[0]);
                usuarioService.atualizarPerfil(atualizado, usuario);
                FxDialogs.info("Perfil atualizado.");
                atualizar.run();
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        });
        card.getChildren().addAll(FxAssets.view(FxAssets.imageFromPathOrAsset(usuario.getAvatarPath(), FxAssets.AVATAR), 120, 120),
                nome, email, curso, turma, avatarButton, salvar);
        root.getChildren().add(card);
        return scroll(root);
    }

    public static Node relatorios(Usuario usuario) {
        VBox root = telaBase();
        root.getChildren().add(FxTheme.title("Relatórios", 22));
        PostService postService = new PostService();
        ChamadoService chamadoService = new ChamadoService();
        EventoService eventoService = new EventoService();
        DisciplinaService disciplinaService = new DisciplinaService();
        UsuarioService usuarioService = new UsuarioService();
        FlowPane cards = new FlowPane(14, 14);
        cards.getChildren().addAll(
                relatorio("Posts visíveis", seguro(() -> postService.contarPostsVisiveis(usuario))),
                relatorio("Chamados abertos", seguro(() -> chamadoService.contarAbertos(usuario))),
                relatorio("Eventos próximos", seguro(() -> eventoService.contarProximos(usuario))),
                relatorio("Disciplinas cadastradas", seguro(() -> disciplinaService.contarDisponiveis(usuario)))
        );
        if (usuarioService.podeAdministrarContas(usuario)) {
            cards.getChildren().add(relatorio("Contas pendentes", seguro(() -> usuarioService.contarContasPendentes(usuario))));
        }
        root.getChildren().add(cards);
        return scroll(root);
    }

    public static Node contas(Usuario usuario, Runnable atualizar) {
        VBox root = telaBase();
        UsuarioService service = new UsuarioService();

        Label titulo = FxTheme.title("Administração de contas", 22);
        Label instrucao = FxTheme.muted("Aprove ou rejeite os usuários que fizeram cadastro e ainda estão aguardando liberação.");
        Label resumo = FxTheme.muted("");

        ListView<Usuario> lista = new ListView<>();
        lista.setPrefHeight(430);
        lista.setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-control-inner-background: "
                + FxTheme.CARD + "; -fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 8;");
        lista.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome() + "  •  " + item.getEmail() + "  •  " + item.getTipoPerfil());
                setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-text-fill: " + FxTheme.TEXTO + "; "
                        + "-fx-padding: 12; -fx-border-color: transparent transparent " + FxTheme.BORDA + " transparent;");
            }
        });

        VBox detalhes = FxTheme.card(8);
        detalhes.setPrefWidth(360);
        Label detalheTitulo = FxTheme.title("Detalhes da solicitação", 17);
        Label detalheNome = texto("Selecione uma conta pendente para ver os dados.");
        Label detalheEmail = FxTheme.muted("");
        Label detalhePerfil = FxTheme.muted("");
        Label detalheCurso = FxTheme.muted("");
        Label detalheTurma = FxTheme.muted("");
        Label detalheData = FxTheme.muted("");
        detalhes.getChildren().addAll(detalheTitulo, detalheNome, detalheEmail, detalhePerfil, detalheCurso, detalheTurma, detalheData);

        Button aprovar = FxTheme.primaryButton("Aprovar selecionado");
        Button inativar = FxTheme.secondaryButton("Rejeitar/Inativar");
        Button atualizarLista = FxTheme.secondaryButton("Atualizar lista");
        inativar.setStyle(inativar.getStyle() + "-fx-text-fill: " + FxTheme.ALERTA + ";");
        aprovar.setDisable(true);
        inativar.setDisable(true);

        Runnable limparDetalhes = () -> {
            detalheNome.setText("Selecione uma conta pendente para ver os dados.");
            detalheEmail.setText("");
            detalhePerfil.setText("");
            detalheCurso.setText("");
            detalheTurma.setText("");
            detalheData.setText("");
            aprovar.setDisable(true);
            inativar.setDisable(true);
        };

        lista.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            if (selecionado == null) {
                limparDetalhes.run();
                return;
            }
            detalheNome.setText(selecionado.getNome());
            detalheEmail.setText("E-mail: " + valor(selecionado.getEmail(), "-"));
            detalhePerfil.setText("Perfil solicitado: " + valor(selecionado.getTipoPerfil(), "-"));
            detalheCurso.setText("Curso: " + valor(selecionado.getCurso(), "Não informado"));
            detalheTurma.setText("Turma: " + valor(selecionado.getTurma(), "Não informada"));
            detalheData.setText("Cadastro realizado em: " + data(selecionado.getCreatedAt()));
            aprovar.setDisable(false);
            inativar.setDisable(false);
        });

        Runnable carregar = () -> {
            try {
                List<Usuario> pendentes = service.listarContasPendentes(usuario);
                lista.getItems().setAll(pendentes);
                lista.getSelectionModel().clearSelection();
                limparDetalhes.run();
                if (pendentes.isEmpty()) {
                    resumo.setText("Nenhuma conta pendente no momento.");
                } else if (pendentes.size() == 1) {
                    resumo.setText("1 conta aguardando aprovação.");
                } else {
                    resumo.setText(pendentes.size() + " contas aguardando aprovação.");
                }
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        };

        atualizarLista.setOnAction(e -> carregar.run());

        aprovar.setOnAction(e -> {
            Usuario selecionado = lista.getSelectionModel().getSelectedItem();
            if (selecionado == null) {
                FxDialogs.info("Selecione uma conta antes de aprovar.");
                return;
            }
            if (!FxDialogs.confirmar("Aprovar a conta de " + selecionado.getNome() + "?")) {
                return;
            }
            try {
                service.aprovarUsuario(selecionado.getIdUsuario(), usuario);
                notificarAprovacaoConta(selecionado);
                FxDialogs.info("Conta aprovada com sucesso.");
                carregar.run();
                atualizar.run();
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        });

        inativar.setOnAction(e -> {
            Usuario selecionado = lista.getSelectionModel().getSelectedItem();
            if (selecionado == null) {
                FxDialogs.info("Selecione uma conta antes de rejeitar ou inativar.");
                return;
            }
            if (!FxDialogs.confirmar("Rejeitar/Inativar a conta de " + selecionado.getNome() + "?")) {
                return;
            }
            try {
                service.inativarUsuario(selecionado.getIdUsuario(), usuario);
                FxDialogs.info("Conta rejeitada/inativada com sucesso.");
                carregar.run();
                atualizar.run();
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        });

        HBox acoes = new HBox(8);
        acoes.setAlignment(Pos.CENTER_LEFT);
        acoes.getChildren().addAll(aprovar, inativar, atualizarLista);

        HBox conteudo = new HBox(14);
        HBox.setHgrow(lista, Priority.ALWAYS);
        conteudo.getChildren().addAll(lista, detalhes);

        root.getChildren().addAll(titulo, instrucao, resumo, conteudo, acoes);
        carregar.run();
        return scroll(root);
    }

    public static Node configuracoes(Usuario usuario) {
        VBox root = telaBase();
        root.getChildren().add(FxTheme.title("Configurações", 22));
        VBox card = FxTheme.card(10);
        CheckBox feed = new CheckBox("Notificações de feed");
        feed.setSelected(true);
        CheckBox chamados = new CheckBox("Notificações de chamados");
        chamados.setSelected(true);
        ComboBox<String> densidade = new ComboBox<>();
        densidade.getItems().addAll("CONFORTAVEL", "COMPACTA");
        densidade.setValue("CONFORTAVEL");
        densidade.setStyle(FxTheme.inputStyle());
        Button salvar = FxTheme.primaryButton("Salvar preferências");
        salvar.setOnAction(e -> FxDialogs.info("Preferências salvas para esta sessão."));
        card.getChildren().addAll(FxTheme.muted("Configurações visuais e notificações internas."), feed, chamados,
                new Label("Densidade da interface"), densidade, salvar);
        root.getChildren().add(card);
        return scroll(root);
    }

    private static void trocarConteudo(VBox conteudo, Node node) {
        VBox.setVgrow(node, Priority.ALWAYS);
        if (node instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }
        conteudo.getChildren().setAll(node);
    }

    private static void selecionarAba(Button ativo, Button... inativos) {
        ativo.setStyle("-fx-background-color: " + FxTheme.AZUL + "; -fx-text-fill: white; -fx-font-weight: 800; "
                + "-fx-background-radius: 8; -fx-padding: 9 16;");
        for (Button botao : inativos) {
            botao.setStyle("-fx-background-color: #22303C; -fx-text-fill: #E7EDF2; "
                    + "-fx-border-color: #536471; -fx-border-radius: 8; -fx-background-radius: 8; "
                    + "-fx-font-weight: 700; -fx-padding: 8 12;");
        }
    }

    private static VBox telaBase() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(18, 22, 22, 22));
        root.setStyle("-fx-background-color: " + FxTheme.FUNDO + ";");
        return root;
    }

    private static ScrollPane scroll(VBox root) {
        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    private static TextField campo(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(38);
        field.setStyle(FxTheme.inputStyle());
        return field;
    }

    private static ComboBox<String> comboNumerico(int inicio, int fim, int passo, String valorInicial) {
        ComboBox<String> combo = new ComboBox<>();
        for (int valor = inicio; valor <= fim; valor += passo) {
            combo.getItems().add(String.format("%02d", valor));
        }
        combo.setValue(valorInicial);
        combo.setPrefHeight(38);
        combo.setStyle(FxTheme.inputStyle());
        return combo;
    }

    private static TextArea area(String prompt) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefRowCount(3);
        area.setWrapText(true);
        area.setStyle(FxTheme.inputStyle());
        return area;
    }

    private static Label texto(String texto) {
        Label label = new Label(texto);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + FxTheme.TEXTO + "; -fx-font-size: 14px;");
        return label;
    }

    private static VBox empty(String mensagem, String asset) {
        VBox box = FxTheme.card(8);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(FxAssets.view(asset, 260, 160), FxTheme.muted(mensagem));
        return box;
    }


    private static VBox cardNotificacao(Notificacao notificacao) {
        VBox card = FxTheme.card(8);

        HBox topo = new HBox(10);
        topo.setAlignment(Pos.CENTER_LEFT);
        Label titulo = FxTheme.title(valor(notificacao.getTitulo(), "Notificação"), 17);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label status = FxTheme.muted(notificacao.isLida() ? "Lida" : "Não lida");
        if (!notificacao.isLida()) {
            status.setStyle("-fx-text-fill: " + FxTheme.VERDE + "; -fx-font-size: 12px; -fx-font-weight: 800;");
        }
        topo.getChildren().addAll(titulo, spacer, status);

        Label tipo = FxTheme.muted("Tipo: " + valor(notificacao.getTipo(), "-")
                + "  •  Destino: " + valor(notificacao.getDestino(), "-")
                + "  •  " + data(notificacao.getCreatedAt()));

        card.getChildren().addAll(topo, tipo, texto(valor(notificacao.getMensagem(), "Sem mensagem.")));
        return card;
    }

    private static void notificarAprovacaoConta(Usuario usuarioAprovado) {
        if (usuarioAprovado == null || usuarioAprovado.getIdUsuario() == null) {
            return;
        }
        try {
            new NotificacaoService().notificarUsuario(
                    usuarioAprovado.getIdUsuario(),
                    "Conta aprovada",
                    "Sua conta foi aprovada. Agora você já pode acessar o ComunicAluno normalmente.",
                    "CONTA",
                    "PERFIL"
            );
        } catch (Exception ignored) {
        }
    }

    private static VBox relatorio(String titulo, int valor) {
        VBox card = FxTheme.card(8);
        card.setPrefWidth(230);
        card.getChildren().addAll(FxTheme.muted(titulo), FxTheme.title(String.valueOf(valor), 28));
        return card;
    }

    private static String valor(String valor, String fallback) {
        return valor == null || valor.isBlank() ? fallback : valor;
    }

    private static String data(LocalDateTime data) {
        if (data == null) {
            return "-";
        }
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private static int seguro(Contagem contagem) {
        try {
            return contagem.executar();
        } catch (Exception ex) {
            return 0;
        }
    }

    private interface Contagem {
        int executar();
    }
}
