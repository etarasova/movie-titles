import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws Exception {

        //read in csv-file and creates list with movies
        List<Movie> movies = readMovies(new File("data/movies.csv"));

        //create a binary search tree
        MovieTree movieTree = new MovieTree();
        movies.forEach(movieTree::insert);

        //print tree in console
        movieTree.printTree();

        //show some subsets and save them in output files
        Set<Movie> sample1 = movieTree.subSet("Back to the Future", "Hulk");
        writeCSVFile("output/sample1", sample1);

        Set<Movie> sample2 = movieTree.subSet("Toy Story", "Walking and Talking");
        writeCSVFile("output/sample2", sample2);

        Set<Movie> sample3 = movieTree.subSet("Catwalk", "Hoax");
        writeCSVFile("output/sample3", sample3);
    }

    private static List<Movie> readMovies(File fileName) throws Exception {
        List<Movie> movies = new LinkedList<>();
        List<String[]> movieLines = readCSVFile(fileName);
        for (String[] movieLine : movieLines) {
            movies.add(Movie.parseLine(movieLine));
        }
        return movies;
    }

    //read csv file using openCSV library
    private static List<String[]> readCSVFile(File filename) throws IOException, CsvException {
        //Start reading from line number 1 (line numbers start from zero)
        CSVReader reader = new CSVReaderBuilder(new FileReader(filename))
                .withSkipLines(1)
                .build();
        //Read all rows at once
        return reader.readAll();
    }

    //write artist ratings to file
    private static void writeCSVFile(String filename, Set<Movie> sample) throws IOException {
        List<String[]> list = new LinkedList<>();
        sample.forEach(movie -> list.add(movie.toRows()));
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            writer.writeNext(new String[]{"Movie ID", "Title", "Genres"});
            writer.writeAll(list);
        }
    }
}

class Movie implements Comparable<Movie> {
    private static final Pattern TITLE_PATTERN = Pattern.compile("^(.+)\\s\\(?(\\d{4})\\)?$");

    private int movieID;
    private String title;
    private int releasedYear;
    private String[] genres;

    public static Movie parseLine(String[] line) {
        Movie movie = new Movie();
        movie.fromLine(line);
        return movie;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getReleasedYear() {
        return releasedYear;
    }

    public void setReleasedYear(int releasedYear) {
        this.releasedYear = releasedYear;
    }

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public void fromLine(String[] line) {
        setMovieID(Integer.parseInt(line[0]));
        setTitleAndReleaseYear(line[1], getMovieID());
        setGenres(line[2].split("\\|"));
    }

    public String[] toRows() {
        String[] rows = new String[3];
        rows[0] = String.valueOf(getMovieID());
        rows[1] = getTitle();
        rows[2] = String.join("|", getGenres());
        return rows;
    }

    private void setTitleAndReleaseYear(String titleAndYear, int movieId) {
        Matcher matcher = TITLE_PATTERN.matcher(titleAndYear.trim());
        setTitle(titleAndYear + " - " + movieId);
        if (matcher.find()) {
            setReleasedYear(Integer.parseInt(matcher.group(2)));
        } else {
            //System.out.println("Invalid title: " + titleAndYear);
            setReleasedYear(9999);
        }
    }

    boolean isWithinRange(String start, String end) {
        return (getTitle().compareTo(start) >= 0 && getTitle().compareTo(end) <= 0);
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Movie)) {
            return false;
        }
        Movie other = (Movie) obj;
        return Objects.equals(this.getMovieID(), other.getMovieID());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getMovieID());
    }

    @Override
    public int compareTo(Movie other) {
        return this.getTitle().compareTo(other.getTitle());
    }
}

class MovieTree {
    MovieNode root;

    public void insert(Movie movie) {
        this.root = insert(this.root, movie);
    }

    public MovieNode insert(MovieNode node, Movie movie) {
        if (node == null) {
            return new MovieNode(movie);
        }

        if (node.getMovie().compareTo(movie) > 0) {
            node.setLeft(insert(node.getLeft(), movie));
        } else if (node.getMovie().compareTo(movie) < 0) {
            node.setRight(insert(node.getRight(), movie));
        } else {
            throw new IllegalArgumentException("Duplicate movie: " + movie);
        }

        return node;
    }

    public Set<Movie> subSet(String start, String end) {
        Set<Movie> movies = new LinkedHashSet<>();
        filterNode(root, start, end, movies);
        return movies;
    }

    private void filterNode(MovieNode node, String start, String end, Set<Movie> movies) {
        if (node == null) {
            return;
        }

        filterNode(node.getLeft(), start, end, movies);

        if (node.getMovie().isWithinRange(start, end)) {
            movies.add(node.getMovie());
        }
        ;

        filterNode(node.getRight(), start, end, movies);
    }

    public void printTree() {
        printNode(root);
    }

    private void printNode(MovieNode node) {
        if (node == null) {
            return;
        }

        printNode(node.getLeft());
        System.out.println(node);
        printNode(node.getRight());
    }
}

class MovieNode {
    private MovieNode left;
    private MovieNode right;
    private Movie movie;

    MovieNode(Movie movie) {
        this.movie = movie;
    }

    public Movie getMovie() {
        return movie;
    }

    public MovieNode getLeft() {
        return left;
    }

    public void setLeft(MovieNode left) {
        this.left = left;
    }

    public MovieNode getRight() {
        return right;
    }

    public void setRight(MovieNode right) {
        this.right = right;
    }

    public String toString() {
        return getMovie().toString();
    }
}