import javax.swing.*;
import java.awt.*;

//DIMENSIONI FISSATE PER POSTO AUTO, CONSIDERATO COME RETTANGOLO 2.5m * 5.0m.
//NEL PROGRAMMA CONSIDERATE COME 2.5*10 E 5.0*10 pixel.
//OGNI RETTANGOLO CHE ENTRA COMPLETAMENTE NELL FIGURA GEOMETRICA PRESA IN ESAME E' UN POTENZIALE PARCHEGGIO.

class DrawGrid extends JPanel {
    private void doDrawing(Graphics g) {
        var geometricShape = (Graphics2D) g;
        geometricShape.setColor(Color.blue);
        var size = getSize();
        var insets = getInsets();

        int widht = size.width - insets.left - insets.right;
        int height = size.height - insets.top - insets.bottom;

        //PARCHEGGIO CONSIDERATO COME RETTANGOLO CHE PRENDE LE MISURE DELL'INTERA FINESTRA VISUALIZZATA
        //SI CONSIDERANO DUE CASI: GRIGLIA VERTICALE E GRIGLIA ORIZZONTALE
        geometricShape.drawRect(0,0,widht,height);
        geometricShape.fillRect(0,0,widht,height);

        //Griglia verticale
        //Righe orizzontali
        geometricShape.setColor(Color.black);
        for (int i = 0; i <= height; i+=5.0 * 10) {
            geometricShape.drawLine(0, i, widht, i);
        }
        //Righe verticali
        for (int i = 0; i <= widht; i+=2.5 * 10) {
            geometricShape.drawLine(i, 0, i, height);
        }


        //Griglia orizzontale
        //Righe orizzontali
        /*geometricShape.setColor(Color.black);
        for (int i = 0; i <= height; i+=2.5 * 10) {
            geometricShape.drawLine(0, i, widht, i);
        }
        //Righe verticali
        for (int i = 0; i <= widht; i+=5.0 * 10) {
            geometricShape.drawLine(i, 0, i, height);
        }*/

        //PARCHEGGIO CONSIDERATO COME QUADRATO CENTRATO NEL CENTRO DELLA FINESTRA
        //DIMENSIONE LATO QUADRATO 30*10 PIXEL.
        //geometricShape.drawRect((widht/2)-15*10,(height/2) -15*10,30*10,30*10);
        //geometricShape.fillRect((widht/2)-15*10,(height/2) -15*10,30*10,30*10);
        //geometricShape.setColor(Color.black);

        //Griglia Orizzontale
        //Righe Orizzontali
        /*for (int i = (height/2) -15*10; i <= (height/2) +15*10; i+=2.5 * 10) {
            geometricShape.drawLine((widht/2)-15*10, i, (widht/2)+15*10, i);
        }
        //Righe Verticali
        for (int i = (widht/2)-15*10; i <= (widht/2)+15*10; i+=5.0 * 10) {
            geometricShape.drawLine(i, (height/2)-15*10, i, (height/2)+15*10);
        }*/

        //Griglia Verticale
        //Righe Orizzontali
        /*for (int i = (height/2) -15*10; i <= (height/2) +15*10; i+=5.0 * 10) {
            geometricShape.drawLine((widht/2)-15*10, i, (widht/2)+15*10, i);
        }
        //Righe Verticali
        for (int i = (widht/2)-15*10; i <= (widht/2)+15*10; i+=2.5 * 10) {
            geometricShape.drawLine(i, (height/2)-15*10, i, (height/2)+15*10);
        }*/

        //PARCHEGGIO CONSIDERATO COME TRIANGOLO CENTRATO NEL CENTRO DELLA FINESTRA
        //DIMENSIONE BASE 30*10 PIXEL, DIMENSIONE ALTEZZA 30*10 PIXEL
        //geometricShape.drawPolygon(new int[] {widht/2, (widht/2)-15*10, (widht/2)+15*10}, new int[] {height/2-15*10, (height/2)+15*10, (height/2)+15*10}, 3);

        //Griglia Orizzontale
        //Righe Orizzontali
        /*for (int i = (height/2) -15*10; i <= (height/2) +15*10; i+=2.5 * 10) {
            geometricShape.drawLine((widht/2)-15*10, i, (widht/2)+15*10, i);
        }
        //Righe Verticali
        for (int i = (widht/2)-15*10; i <= (widht/2)+15*10; i+=5.0 * 10) {
            geometricShape.drawLine(i, (height/2)-15*10, i, (height/2)+15*10);
        }*/

        //Griglia Verticale
        //Righe Orizzontali
        /*for (int i = (height/2) -15*10; i <= (height/2) +15*10; i+=5.0 * 10) {
            geometricShape.drawLine((widht/2)-15*10, i, (widht/2)+15*10, i);
        }
        //Righe Verticali
        for (int i = (widht/2)-15*10; i <= (widht/2)+15*10; i+=2.5 * 10) {
            geometricShape.drawLine(i, (height/2)-15*10, i, (height/2)+15*10);
        }*/
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }
}

public class Main extends JFrame {
    public Main() {
        initUI();
    }
    private void initUI() {
        var drawGrid = new DrawGrid();
        add(drawGrid);

        setSize(500, 400);
        setTitle("TESI");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            var main = new Main();
            main.setVisible(true);
        });
    }
}