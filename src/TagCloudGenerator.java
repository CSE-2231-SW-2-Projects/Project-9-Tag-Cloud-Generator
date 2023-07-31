import java.util.Comparator;

import components.map.Map;
import components.map.Map.Pair;
import components.map.Map1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine1L;

/**
 * @note I am doing Additional Activities 2 and so the program will be
 *       "case-insensitive". <pre>
 *
 * Tag Cloud Generator project: A program that counts word occurrences in a user
 * given input file and outputs an HTML file with a tag cloud of the words.
 *
 * </pre>
 *
 * @author Zheyuan Gao
 * @author Cedric Fausey
 */
public final class TagCloudGenerator {

    /**
     * Default constructor--private to prevent instantiation.
     */
    private TagCloudGenerator() {
        // no code needed here
    }

    /**
     * Override the comparator to compare the alphabet of keys of two pairs in a
     * map.
     *
     * @ensure return negative number if first pair's key is in front of the
     *         second pair's key first character in alphabet order. 0 if
     *         pair1.equals(pair2). Positive number otherwise.(Ignore cases).
     */
    public static class KeyLT implements Comparator<Map.Pair<String, Integer>> {

        @Override
        public final int compare(Pair<String, Integer> pair1,
                Pair<String, Integer> pair2) {
            int compare;
            if (pair1.equals(pair2)) {
                //(pair1.compareTo(pair2) == 0) == pair1.equals(pair2)
                compare = 0;
            } else {
                compare = pair1.key().toLowerCase()
                        .compareTo(pair2.key().toLowerCase());
                if (compare == 0) {
                    /*
                     * If only the key of two pairs are equal, we cannot just
                     * return 0 since they may not have the same value. It does
                     * not matter which pair goes first since they have the same
                     * key. Return the result of comparing two pairs' values.
                     */
                    compare = pair1.value().compareTo(pair2.value());
                }
            }
            return compare;
        }

    }

    /**
     * Override the comparator to compare the values of two pairs in a map.
     *
     * @ensure return positive number if first pair's value is less than the
     *         second pair's value. 0 if pair1.equals(pair2). negative number
     *         otherwise. The order is decreasing order.
     */
    public static class ValueLT
            implements Comparator<Map.Pair<String, Integer>> {

        @Override
        public final int compare(Pair<String, Integer> pair1,
                Pair<String, Integer> pair2) {
            int compare;
            if (pair1.equals(pair2)) {
                //(pair1.compareTo(pair2) == 0) == pair1.equals(pair2)
                compare = 0;
            } else {
                compare = pair2.value().compareTo(pair1.value());
                if (compare == 0) {
                    /*
                     * If only the value of two pairs are equal, we cannot just
                     * return 0 since they may not have the same key. It does
                     * not matter which pair goes first since they have the same
                     * value. Return the result of comparing two pairs' keys.
                     */
                    compare = pair2.key().toLowerCase()
                            .compareTo(pair1.key().toLowerCase());
                }
            }
            return compare;
        }

    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    public static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        char c = text.charAt(position);
        boolean isSeparator = separators.contains(c);

        StringBuilder subString = new StringBuilder();
        int endIndex = position;
        boolean end = false;
        /*
         * if the character is not a separator, then return the substring start
         * from position to the appearance of the next separator
         */
        if (!isSeparator) {
            while (!end && endIndex < text.length()) {

                if (!separators.contains(text.charAt(endIndex))) {
                    subString.append(text.charAt(endIndex));
                } else {
                    end = true;
                }
                endIndex++;

            }

        } else {
            /*
             * if the character is a separator, then return the substring start
             * at position and end at the first appearance of the character that
             * not in the separator set.
             */
            while (!end && endIndex < text.length()) {

                if (separators.contains(text.charAt(endIndex))) {
                    subString.append(text.charAt(endIndex));
                } else {
                    end = true;
                }
                endIndex++;
            }

        }
        return subString.toString();
    }

    /**
     * Read the input file and store all the words and the time of appearance in
     * a map.
     *
     * @param input
     *            Simple Reader to read the user input file
     * @param wordCount
     *            Map to store the words and the number of appearance in the
     *            give file
     * @param separator
     *            set that contains all the separator characters
     * @require [Input is open. The user file exists]
     * @ensure [The map will contains the words in the file and the
     *         corresponding times it appears]
     */
    public static void readFileConvertToMap(SimpleReader input,
            Map<String, Integer> wordCount, Set<Character> separator) {
        while (!input.atEOS()) {
            String nextLine = input.nextLine();
            /*
             * Introduce the integer value startPosition to keep track the
             * process of recording the words in the sentence to the map
             */
            int startPosition = 0;
            /*
             * We process can only proceed when the start position is in the
             * scope of the given sentence
             */
            while (startPosition < nextLine.length()) {
                String token = nextWordOrSeparator(nextLine, startPosition,
                        separator);
                /*
                 * If the token is a word
                 */
                if (!separator.contains(token.charAt(0))) {
                    //Convert the word to lower case to make the program case-insensitive.
                    token = token.toLowerCase();
                    /*
                     * if the map does not have this word yet, add it to the
                     * map.
                     */
                    if (!wordCount.hasKey(token)) {
                        wordCount.add(token, 1);

                    } else {
                        /*
                         * if the word has already exist, add one to its
                         * corresponding value in map
                         */
                        wordCount.replaceValue(token,
                                wordCount.value(token) + 1);
                    }
                }
                /*
                 * Update the start position and contain to check next token
                 */
                startPosition += token.length();
            }
        }

    }

    /**
     * Arrange all the keys in the map into the ValueLT sorting machine. And
     * then put first n elements into KeyLT sorting machine. Return the KeyLT
     * sorting machine in extraction mode.
     *
     * @param wordCount
     *            The map to store the word and corresponding numbers of
     *            appearance in the content
     * @param n
     *            The number of words I need to generate the tag cloud.
     * @param keySort
     *            The sorting machine to sort the words in alphabetical order.
     * @require [n >= 0, and n <= wordCount.size]
     * @ensure [Sorting machine that contains n most appearance words in KeyLT
     *         order and in Extraction Mode]
     */
    public static void putWordsInSortingMachine(Map<String, Integer> wordCount,
            int n, SortingMachine<Map.Pair<String, Integer>> keySort) {
        assert n >= 0 : "violation of n >= 0.";
        assert n <= wordCount.size() : "violation of n <= wordCount.size.";

        /*
         * First put all the pairs in the wordCount into the sorting machine to
         * sort the values from large to small.
         */
        SortingMachine<Map.Pair<String, Integer>> valueSort = new SortingMachine1L<>(
                new ValueLT());
        while (wordCount.size() > 0) {
            valueSort.add(wordCount.removeAny());
        }
        valueSort.changeToExtractionMode();

        /*
         * Arrange the first n pairs in the valueSort in to another sorting
         * machine sorted in alphabetical order.
         */
        int count = 0;
        while (count < n) {
            keySort.add(valueSort.removeFirst());
            count++;
        }
        keySort.changeToExtractionMode();

    }

    /**
     * Generate the corresponding HTML file to the user choice location.
     *
     * @param output
     *            The simple writer to output content to the user choice
     *            location
     * @param file
     *            The location of the user given text
     * @param keySort
     *            The sorting machine contains the pairs' key in alphabetical
     *            order.
     * @require [The output is open.]
     * @ensure [Output the file to same location as the user input file. The
     *         file contains the top appearance of first n numbers of words' Tag
     *         Cloud in HTML format.]
     */
    public static void outputHTML(SimpleWriter output, String file,
            SortingMachine<Map.Pair<String, Integer>> keySort) {

        //Declare the minimum and max display font size for tag cloud.
        final int minSize = 11;
        final int maxSize = 48;

        /*
         * output the header of the file
         */
        output.println("<html>");
        output.println(" <head>");
        output.println("  <title>Top " + keySort.size() + " words in " + file
                + "</title>");
        output.print(
                "  <link href=\"http://web.cse.ohio-state.edu/software/2231/web-"
                        + "sw2/assignments/projects/tag-cloud-generator/data/"
                        + "tagcloud.css\"");
        output.println("rel=\"stylesheet\" type=\"text/css\">");
        output.println(" </head>");
        output.println(" <body>");
        output.println(
                "  <h2>Top " + keySort.size() + " words in " + file + "</h2>");
        output.println("  <hr>");
        output.println("   <div class=\"cdiv\">");
        output.println("    <p class=\"cbox\">");

        /*
         * output the body of the file
         */
        /*
         * First we need to find max count and minimum count in order to
         * calculate the display font size.
         */
        int maxCount = 0;
        int minCount = 0;
        for (Map.Pair<String, Integer> pair : keySort) {
            if (minCount == 0) {
                minCount = pair.value();
            }
            if (pair.value() > maxCount) {
                maxCount = pair.value();
            } else if (pair.value() < minCount) {
                minCount = pair.value();
            }
        }

        while (keySort.size() > 0) {
            Map.Pair<String, Integer> wordCount = keySort.removeFirst();
            //Calculate the display size for this word.
            double ratio = (double) (wordCount.value() - minCount)
                    / (maxCount - minCount);
            int size = minSize + (int) (ratio * (maxSize - minSize));
            //Output the corresponding HTML format.
            output.println("     <span style=\"cursor:default\" class=\"f"
                    + size + "\" title=\"count: " + wordCount.value() + "\">"
                    + wordCount.key() + "</span>");

        }
        /*
         * output the footer of the file
         */
        output.println("    </p>");
        output.println("   </div>");
        output.println(" </body>");
        output.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        /*
         * Generate a set of separators
         */
        Set<Character> separatorSet = new Set1L<>();
        separatorSet.add('\n');
        separatorSet.add('\t');
        separatorSet.add('\r');
        separatorSet.add(',');
        separatorSet.add('-');
        separatorSet.add(' ');
        separatorSet.add('.');
        separatorSet.add('?');
        separatorSet.add('!');
        separatorSet.add(':');
        separatorSet.add(';');
        separatorSet.add('/');
        separatorSet.add('\'');
        separatorSet.add('"');
        separatorSet.add('[');
        separatorSet.add(']');
        separatorSet.add('(');
        separatorSet.add(')');
        separatorSet.add('*');
        separatorSet.add('_');
        separatorSet.add('~');

        /*
         * Open input and output streams
         */
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Ask user for file location and output location
         */
        out.println();
        out.print("Please input the name of the input file: ");
        String file = in.nextLine();
        out.print("Please enter the name of the output file: ");
        String folder = in.nextLine();
        SimpleReader input = new SimpleReader1L(file);
        SimpleWriter output = new SimpleWriter1L(folder);
        /*
         * Store the text data in the map.
         */
        Map<String, Integer> wordCount = new Map1L<>();
        readFileConvertToMap(input, wordCount, separatorSet);
        /*
         * Ask user for number n. n has to be an positive integer and n cannot
         * be larger than the size of word map.
         */
        out.print("Enter the number of words (in the range of 0 to "
                + wordCount.size()
                + ") you would like to have in your tag cloud: ");
        int n = in.nextInteger();
        while (n < 0 || n > wordCount.size()) {
            out.println("Please enter a valid integer! n has to be an positive "
                    + "integer and n cannot be larger than the size of word map.");
            out.print("Enter the number of words (in the range of 0 to "
                    + wordCount.size()
                    + ") you would like to have in your tag cloud: ");
            n = in.nextInteger();
        }
        /*
         * Generate the HTML file to user choose location
         */
        SortingMachine<Map.Pair<String, Integer>> keySort = new SortingMachine1L<>(
                new KeyLT());
        putWordsInSortingMachine(wordCount, n, keySort);
        outputHTML(output, file, keySort);
        /*
         * Close the input and output streams
         */
        in.close();
        out.close();
        input.close();
        output.close();
    }

}
