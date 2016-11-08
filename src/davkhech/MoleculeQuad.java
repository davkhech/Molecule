package davkhech;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

class Atom {
    private int _x, _y;

    Atom(int x, int y) {
        _x = x;
        _y = y;
    }

    int getX() {
        return _x;
    }

    int getY() {
        return _y;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Atom && _x == ((Atom) obj).getX() && _y == ((Atom) obj).getY();
    }
}

class Molecule {
    private Atom atom;
    private Molecule prev;

    Molecule(Atom atom, Molecule molecule) {
        this.atom = atom;
        this.prev = molecule;
    }

    double getEnergy(double eps0, double eps1) {
        ArrayList<Atom> atoms = (ArrayList<Atom>) this.getAtoms();
        double e = .0;
        HashSet<Atom> set = new HashSet<>();
        for (int i = 1; i < atoms.size(); ++i) {
            if (atoms.get(i).getY() == atoms.get(i - 1).getY() && atoms.get(i).getY() == 0) {
                e += eps0;
                set.add(atoms.get(i));
                set.add(atoms.get(i - 1));
            }
        }
        for (Atom atom : atoms) {
            if (atom.getY() == 0 && !set.contains(atom))
                e += eps1;
        }
        return e;
    }

    double getRadius() {
        return Math.sqrt(Math.pow(atom.getX(), 2.0) + Math.pow(atom.getY(), 2.0));
    }

    double getRadiusSqr() {
        return Math.pow(atom.getX(), 2.0) + Math.pow(atom.getY(), 2.0);
    }

    String getMoleculeString() {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Atom> atoms = (ArrayList<Atom>) this.getAtoms();
        atoms.forEach((atom) ->
                stringBuilder.append("(").append(atom.getX()).append(", ").append(atom.getY()).append(")")
        );
        stringBuilder.append('\n');
        return stringBuilder.toString();
    }

    private List<Atom> getAtoms() {
        if (prev == null) {
            ArrayList<Atom> atoms = new ArrayList<>();
            atoms.add(atom);
            return atoms;
        }
        ArrayList<Atom> list = (ArrayList<Atom>) prev.getAtoms();
        list.add(0, atom);
        return list;
    }
}

class Solver {
    private PrintWriter writer;
    private ArrayList<Atom> list = new ArrayList<>();
    private double radiusSum = 0;
    private double energySum = 0;
    private double energySqrSum = 0;
    private double energyNorm = 0;

    Solver(PrintWriter writer) {
        this.writer = writer;
        list.add(new Atom(-1, 0));
        list.add(new Atom(0, 1));
        list.add(new Atom(1, 0));
        list.add(new Atom(0, -1));
        // by commenting two lines below you can get solution for square coordinate space
//        list.add(new Atom(-1, 1));
//        list.add(new Atom(1, -1));
    }

    double getRadiusSum() {
        return this.radiusSum;
    }

    void setRadiusSum(double radiusSum) {
        this.radiusSum = radiusSum;
    }

    double getEnergySum() {
        return energySum;
    }

    void setEnergySum(double energySum) {
        this.energySum = energySum;
    }

    double getEnergySqrSum() {
        return energySqrSum;
    }

    void setEnergySqrSum(double energySqrSum) {
        this.energySqrSum = energySqrSum;
    }

    double getEnergyNorm() {
        return energyNorm;
    }

    void setEnergyNorm(double energyNorm) {
        this.energyNorm = energyNorm;
    }

    void configure(int x, int y, int len, double T) {
        configure(x, y, len, new Molecule(new Atom(x, y), null), T);
    }

    void configure(int x, int y, int len, Molecule molecule, double T) {
        if (len == 0) {
//            writer.write(molecule.getMoleculeString());
            double dE = molecule.getEnergy(10, 1);
            radiusSum += molecule.getRadiusSqr() * Math.exp(-dE / T);
            energySum += dE * Math.exp(-dE / T);
            energySqrSum += dE * dE * Math.exp(-dE / T);
            energyNorm += Math.exp(-dE / T);
        } else {
            list.forEach((dS) -> configure(x + dS.getX(), y + dS.getY(), len - 1, new Molecule(new Atom(x + dS.getX(), y + dS.getY()), molecule), T));
        }
    }
}

public class MoleculeQuad {

    public static void main(String[] args) {
        PrintWriter writer;
        try {
            writer = new PrintWriter("molecules.txt", "UTF-8");
            Solver solver = new Solver(writer);
            for (int i = 1; i <= 50; ++i) {
                solver.setRadiusSum(0);
                solver.setEnergySum(0);
                solver.setEnergySqrSum(0);
                solver.setEnergyNorm(0);
                solver.configure(0, 0, 10, i);
                double T = i;
                double mE2 = Math.pow(solver.getEnergySum() / solver.getEnergyNorm(), 2);
                double E2m = solver.getEnergySqrSum() / solver.getEnergyNorm();
                double mR2 = solver.getRadiusSum() / solver.getEnergyNorm();
                writer.printf("%f,%f,%f,%f\n", T, mE2, E2m, mR2);
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            System.out.println("I'm finished!");
        }
    }
}