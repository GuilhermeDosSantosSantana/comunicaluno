package br.com.comunicaluno.service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ArquivoService {

    private final Path baseDir;

    public ArquivoService() {
        this.baseDir = Path.of(System.getProperty("user.home"), "ComunicAluno", "uploads");
    }

    public String salvarUpload(File arquivoOrigem, String categoria) {
        if (arquivoOrigem == null) {
            return null;
        }
        if (!arquivoOrigem.exists() || !arquivoOrigem.isFile()) {
            throw new IllegalArgumentException("Arquivo selecionado não encontrado.");
        }

        try {
            Path pastaCategoria = baseDir.resolve(sanitizarCategoria(categoria));
            Files.createDirectories(pastaCategoria);

            String nomeOriginal = arquivoOrigem.getName();
            String extensao = "";
            int ponto = nomeOriginal.lastIndexOf('.');
            if (ponto >= 0 && ponto < nomeOriginal.length() - 1) {
                extensao = nomeOriginal.substring(ponto).replaceAll("[^a-zA-Z0-9.]", "");
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            Path destino = pastaCategoria.resolve(timestamp + "_" + Math.abs(nomeOriginal.hashCode()) + extensao);
            Files.copy(arquivoOrigem.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            removerFundoClaroDePng(destino);
            return destino.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo: " + e.getMessage(), e);
        }
    }

    private void removerFundoClaroDePng(Path arquivo) {
        if (!arquivo.getFileName().toString().toLowerCase().endsWith(".png")) {
            return;
        }

        try {
            BufferedImage original = ImageIO.read(arquivo.toFile());
            if (original == null) {
                return;
            }

            int largura = original.getWidth();
            int altura = original.getHeight();
            BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = imagem.createGraphics();
            g.drawImage(original, 0, 0, null);
            g.dispose();

            boolean[] visitado = new boolean[largura * altura];
            int[] fila = new int[largura * altura];
            int inicio = 0;
            int fim = 0;

            for (int x = 0; x < largura; x++) {
                fim = adicionarFundoClaro(imagem, visitado, fila, fim, x, 0);
                fim = adicionarFundoClaro(imagem, visitado, fila, fim, x, altura - 1);
            }
            for (int y = 0; y < altura; y++) {
                fim = adicionarFundoClaro(imagem, visitado, fila, fim, 0, y);
                fim = adicionarFundoClaro(imagem, visitado, fila, fim, largura - 1, y);
            }

            int removidos = 0;
            while (inicio < fim) {
                int indice = fila[inicio++];
                int x = indice % largura;
                int y = indice / largura;
                int rgb = imagem.getRGB(x, y);
                imagem.setRGB(x, y, rgb & 0x00FFFFFF);
                removidos++;

                fim = adicionarFundoClaro(imagem, visitado, fila, fim, x + 1, y);
                fim = adicionarFundoClaro(imagem, visitado, fila, fim, x - 1, y);
                fim = adicionarFundoClaro(imagem, visitado, fila, fim, x, y + 1);
                fim = adicionarFundoClaro(imagem, visitado, fila, fim, x, y - 1);
            }

            if (removidos < (largura * altura) * 0.03) {
                return;
            }

            BufferedImage cortada = cortarTransparencia(imagem);
            ImageIO.write(cortada, "png", arquivo.toFile());
        } catch (IOException ignored) {
        }
    }

    private int adicionarFundoClaro(BufferedImage imagem, boolean[] visitado, int[] fila, int fim, int x, int y) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        if (x < 0 || y < 0 || x >= largura || y >= altura) {
            return fim;
        }
        int indice = y * largura + x;
        if (visitado[indice]) {
            return fim;
        }
        visitado[indice] = true;
        if (!ehFundoClaro(imagem.getRGB(x, y))) {
            return fim;
        }
        fila[fim] = indice;
        return fim + 1;
    }

    private boolean ehFundoClaro(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
            return true;
        }
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        double saturacao = max == 0 ? 0 : (double) (max - min) / max;
        return (max >= 215 && saturacao <= 0.22) || (max >= 240 && saturacao <= 0.40);
    }

    private BufferedImage cortarTransparencia(BufferedImage imagem) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        int minX = largura;
        int minY = altura;
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int alpha = (imagem.getRGB(x, y) >>> 24) & 0xFF;
                if (alpha > 0) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return imagem;
        }

        int conteudoLargura = maxX - minX + 1;
        int conteudoAltura = maxY - minY + 1;
        int margem = Math.max(10, Math.max(conteudoLargura, conteudoAltura) / 30);
        int x = Math.max(0, minX - margem);
        int y = Math.max(0, minY - margem);
        int direita = Math.min(largura, maxX + margem + 1);
        int baixo = Math.min(altura, maxY + margem + 1);

        BufferedImage cortada = new BufferedImage(direita - x, baixo - y, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cortada.createGraphics();
        g.drawImage(imagem, 0, 0, cortada.getWidth(), cortada.getHeight(), x, y, direita, baixo, null);
        g.dispose();
        return cortada;
    }

    private String sanitizarCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return "geral";
        }
        return categoria.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
