public class Tanque {
    private double capacidadeMaxima;
    private double nivelAtual;
    
    // Construtor
    public Tanque(double capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
        this.nivelAtual = 0.0; // Tanque inicia vazio
    }
    
    // Construtor alternativo com nível inicial
    public Tanque(double capacidadeMaxima, double nivelInicial) {
        this.capacidadeMaxima = capacidadeMaxima;
        setNivelAtual(nivelInicial); // Usa o setter para validar
    }
    
    // Getters
    public double getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
    
    public double getNivelAtual() {
        return nivelAtual;
    }
    
    // Setters
    public void setCapacidadeMaxima(double capacidadeMaxima) {
        if (capacidadeMaxima > 0) {
            this.capacidadeMaxima = capacidadeMaxima;
            // Ajusta o nível atual se exceder a nova capacidade
            if (this.nivelAtual > capacidadeMaxima) {
                this.nivelAtual = capacidadeMaxima;
            }
        }
    }
    
    public void setNivelAtual(double nivelAtual) {
        if (nivelAtual >= 0 && nivelAtual <= capacidadeMaxima) {
            this.nivelAtual = nivelAtual;
        }
    }
    
    // Método para abastecer combustível
    public boolean abastecerCombustivel(double litros) {
        if (litros <= 0) {
            return false; // Não é possível abastecer quantidade negativa ou zero
        }
        
        double novoNivel = nivelAtual + litros;
        
        if (novoNivel <= capacidadeMaxima) {
            nivelAtual = novoNivel;
            return true; // Abastecimento realizado com sucesso
        } else {
            // Abastece até a capacidade máxima
            nivelAtual = capacidadeMaxima;
            return false; // Indica que não foi possível abastecer toda a quantidade
        }
    }
    
    // Método para consumir combustível
    public boolean consumirCombustivel(double litros) {
        if (litros <= 0) {
            return false; // Não é possível consumir quantidade negativa ou zero
        }
        
        if (nivelAtual >= litros) {
            nivelAtual -= litros;
            return true; // Consumo realizado com sucesso
        } else {
            return false; // Não há combustível suficiente
        }
    }
    
    // Métodos auxiliares úteis para o painel
    public double getEspacoDisponivel() {
        return capacidadeMaxima - nivelAtual;
    }
    
    public double getPercentualCombustivel() {
        return (nivelAtual / capacidadeMaxima) * 100;
    }
    
    public boolean estaVazio() {
        return nivelAtual == 0;
    }
    
    public boolean estaCheio() {
        return nivelAtual == capacidadeMaxima;
    }
    
    @Override
    public String toString() {
        return String.format("Tanque: %.2f/%.2f litros (%.1f%%)", 
                           nivelAtual, capacidadeMaxima, getPercentualCombustivel());
    }
}