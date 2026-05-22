package com.forage.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@Service
public class DureeCalculService {

    /**
     * DURÉE ESTIMÉE :
     * Différence brute entre 2 dates, sans contrainte.
     * Le calcul se fait en minutes, puis on arrondit à l'heure.
     */
    public int calculerDureeEstimer(LocalDateTime dateDebut, LocalDateTime dateFin) {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }

        if (dateFin.isBefore(dateDebut)) {
            return 0;
        }

        long totalMinutes = ChronoUnit.MINUTES.between(dateDebut, dateFin);
        return approximationDate(totalMinutes);
    }

    /**
     * DURÉE RÉELLE :
     * Heures de travail effectives entre 2 dates.
     *
     * Contraintes :
     * - Heures de travail : 08:00-12:00 et 13:00-17:00
     * - Samedi et dimanche exclus
     * - Jours fériés exclus
     *
     * Le calcul se fait en minutes puis on arrondit à l'heure.
     */
    public int calculerDureeReel(LocalDateTime dateDebut, LocalDateTime dateFin) {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }

        if (dateFin.isBefore(dateDebut)) {
            return 0;
        }

        long totalMinutesTravail = 0;

        LocalDate dateActuelle = dateDebut.toLocalDate();
        LocalDate dateLimite = dateFin.toLocalDate();

        Set<LocalDate> joursFeries = getJoursFeriesEntreAnnees(
                dateDebut.getYear(),
                dateFin.getYear()
        );

        while (!dateActuelle.isAfter(dateLimite)) {
            if (estJourTravail(dateActuelle, joursFeries)) {
                totalMinutesTravail += calculerMinutesTravailDansJour(dateActuelle, dateDebut, dateFin);
            }

            dateActuelle = dateActuelle.plusDays(1);
        }

        return approximationDate(totalMinutesTravail);
    }

    /**
     * Calcule les minutes de travail pour un jour donné
     * selon les plages :
     * - 08:00 à 12:00
     * - 13:00 à 17:00
     */
    private long calculerMinutesTravailDansJour(LocalDate jour,
                                                LocalDateTime dateDebut,
                                                LocalDateTime dateFin) {

        LocalDateTime matinDebut = jour.atTime(8, 0);
        LocalDateTime matinFin = jour.atTime(12, 59);

        LocalDateTime apremDebut = jour.atTime(13, 0);
        LocalDateTime apremFin = jour.atTime(16, 0);

        long minutesMatin = calculerIntersectionMinutes(dateDebut, dateFin, matinDebut, matinFin);
        long minutesAprem = calculerIntersectionMinutes(dateDebut, dateFin, apremDebut, apremFin);

        return minutesMatin + minutesAprem;
    }

    /**
     * Calcule l'intersection en minutes entre 2 intervalles :
     * [debut1, fin1] et [debut2, fin2]
     */
    private long calculerIntersectionMinutes(LocalDateTime debut1,
                                             LocalDateTime fin1,
                                             LocalDateTime debut2,
                                             LocalDateTime fin2) {

        LocalDateTime debutMax = debut1.isAfter(debut2) ? debut1 : debut2;
        LocalDateTime finMin = fin1.isBefore(fin2) ? fin1 : fin2;

        if (!debutMax.isBefore(finMin)) {
            return 0;
        }

        return ChronoUnit.MINUTES.between(debutMax, finMin);
    }

    /**
     * Vérifie si un jour est ouvrable
     */
    private boolean estJourTravail(LocalDate date, Set<LocalDate> joursFeries) {
        DayOfWeek jour = date.getDayOfWeek();

        if (jour == DayOfWeek.SATURDAY || jour == DayOfWeek.SUNDAY) {
            return false;
        }

        return !joursFeries.contains(date);
    }

    /**
     * Retourne tous les jours fériés entre 2 années incluses
     */
    private Set<LocalDate> getJoursFeriesEntreAnnees(int anneeDebut, int anneeFin) {
        Set<LocalDate> feries = new HashSet<>();

        for (int annee = anneeDebut; annee <= anneeFin; annee++) {
            feries.addAll(getJoursFeries(annee));
        }

        return feries;
    }

    /**
     * Liste des jours fériés à Madagascar
     */
    private Set<LocalDate> getJoursFeries(int annee) {
        Set<LocalDate> feries = new HashSet<>();

        // Jours fériés fixes
        feries.add(LocalDate.of(annee, Month.JANUARY, 1));     // Jour de l'An
        feries.add(LocalDate.of(annee, Month.MARCH, 8));       // Journée de la Femme
        feries.add(LocalDate.of(annee, Month.MARCH, 29));      // Jour des Martyrs
        feries.add(LocalDate.of(annee, Month.MAY, 25));        // Journée de l'OUA
        feries.add(LocalDate.of(annee, Month.JUNE, 26));       // Fête de l'Indépendance
        feries.add(LocalDate.of(annee, Month.AUGUST, 15));     // Assomption
        feries.add(LocalDate.of(annee, Month.NOVEMBER, 1));    // Toussaint
        feries.add(LocalDate.of(annee, Month.DECEMBER, 25));   // Noël

        // Jours fériés mobiles
        LocalDate paquesDimanche = calculerPaques(annee);
        feries.add(paquesDimanche.plusDays(1));   // Lundi de Pâques
        feries.add(paquesDimanche.plusDays(39));  // Ascension
        feries.add(paquesDimanche.plusDays(50));  // Lundi de Pentecôte

        return feries;
    }

    /**
     * Calcul de la date de Pâques (algorithme de Meeus)
     * Retourne le DIMANCHE de Pâques
     */
    private LocalDate calculerPaques(int annee) {
        int a = annee % 19;
        int b = annee / 100;
        int c = annee % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mois = (h + l - 7 * m + 114) / 31;
        int jour = ((h + l - 7 * m + 114) % 31) + 1;

        return LocalDate.of(annee, mois, jour);
    }

    /**
     * Approximation à l'heure :
     * - si minutes >= 30 → arrondi supérieur
     * - sinon → arrondi inférieur
     *
     * Exemple :
     * 125 min = 2h05 -> 2h
     * 150 min = 2h30 -> 3h
     * 179 min = 2h59 -> 3h
     */
    private int approximationDate(long totalMinutes) {
        if (totalMinutes <= 0) {
            return 0;
        }

        long heures = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        return (minutes >= 30) ? (int) (heures + 1) : (int) heures;
    }

    /**
     * Formatage simple d'une durée en heures
     * Exemple : 5h, 12h
     */
    public String formaterHeures(int heures) {
        if (heures <= 0) {
            return "0h";
        }
        return heures + "h";
    }

    /**
     * Formatage en jours ouvrables + heures
     * Base : 8h = 1 jour ouvrable
     *
     * Exemple :
     * 10h -> 1j 2h
     * 16h -> 2j
     * 5h  -> 5h
     */
    public String formaterDureeTravail(int heures) {
        if (heures <= 0) {
            return "0h";
        }

        int jours = heures / 8;
        int heuresRestantes = heures % 8;

        if (jours > 0 && heuresRestantes > 0) {
            return jours + "j " + heuresRestantes + "h";
        } else if (jours > 0) {
            return jours + "j";
        } else {
            return heuresRestantes + "h";
        }
    }

    /**
     * Formatage pour durée brute (estimée) en jours de 24h
     *
     * Exemple :
     * 30h -> 1j 6h
     * 48h -> 2j
     */
    public String formaterDureeBrute(int heures) {
        if (heures <= 0) {
            return "0h";
        }

        int jours = heures / 24;
        int heuresRestantes = heures % 24;

        if (jours > 0 && heuresRestantes > 0) {
            return jours + "j " + heuresRestantes + "h";
        } else if (jours > 0) {
            return jours + "j";
        } else {
            return heuresRestantes + "h";
        }
    }
}