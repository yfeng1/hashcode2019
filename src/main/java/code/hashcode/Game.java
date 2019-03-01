/**
 *  Copyright Murex S.A.S., 2003-2019. All Rights Reserved.
 *
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package code.hashcode;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;


public class Game {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Instance fields
    //~ ----------------------------------------------------------------------------------------------------------------

    public List<Photo> photos = new ArrayList<>();
    public List<Slide> slides = new ArrayList<>();
    public List<Slide> maximum = new ArrayList<>();
    public int globalInteration = 500;
    public int localInteration = 20000;
    public double temperature = 10000.0;
    public float reductTempRatio = 1.02f;
    Random random = new Random(System.currentTimeMillis());

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Constructors
    //~ ----------------------------------------------------------------------------------------------------------------

    public Game() {
        // TODO : add List size
    }

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods
    //~ ----------------------------------------------------------------------------------------------------------------

    public void addPhoto(Photo photo) {
        this.photos.add(photo);
    }

    public void addSlide(Slide slide) {
        this.slides.add(slide);
    }

    public void run() {
        init();
        runCore();
    }

    public void runWithFile(String fileName) throws FileNotFoundException {
        initFromFile(fileName);
        runCore();
    }

    public void runWithFileSwitchRange(String fileName) throws FileNotFoundException {
        initFromFile(fileName);
        runCoreWithReverse();
    }

    public void init() {
        // Init slide
        Photo vPhoto = null;
        for (Photo photo : photos) {
            if (!photo.isVertical()) {
                slides.add(new HorizonSlide(photo));
            } else {
                if (vPhoto == null) {
                    vPhoto = photo;
                } else {
                    slides.add(new VerticalSlide(vPhoto, photo));
                    vPhoto = null;
                }
            }
        }
    }

    public void initFromFile(String fileName) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(fileName));
        int nbSlides = scanner.nextInt();
        scanner.nextLine();

        // Jump the first line
        for (int i = 0; i < nbSlides; i++) {
            String nextLine = scanner.nextLine();
            String[] splited = nextLine.split(" ");
            if (splited.length == 1) {
                slides.add(new HorizonSlide(photos.get(Integer.parseInt(splited[0]))));
            } else if (splited.length == 2) {
                slides.add(new VerticalSlide(photos.get(Integer.parseInt(splited[0])), photos.get(Integer.parseInt(splited[1]))));
            }
        }
    }

    static long evaluate(Slide slide1, Slide slide2) {
        Set<String> tags1 = slide1.getTags();
        Set<String> tags2 = slide2.getTags();

        int common = 0;
        for (String tag : tags2) {
            if (tags1.contains(tag)) {
                common++;
            }
        }

        return Math.min(common, Math.min(tags1.size() - common, tags2.size() - common));
    }

    long deltaReverseRange(int slide1, int slide2) {

        if (slide2 <= slide1) {
            int temp = slide1;
            slide1 = slide2;
            slide2 = temp;
        }

        //point when remove slide1
        long pointToRemove = 0;

        //point when add slide 2 to the position of slide1
        long pointToAdd = 0;

        if (slide1 > 0) {
            if (slide2 == (slides.size() - 1)) {
                pointToAdd += evaluate(slides.get(slide2), slides.get(slide1 - 1)); //
            }
            pointToRemove += evaluate(slides.get(slide1), slides.get(slide1 - 1));
            pointToAdd += evaluate(slides.get(slide2), slides.get(slide1 - 1)); // AE
        }
        if (slide2 < (slides.size() - 1)) {
            if (slide1 == 0) {
                pointToAdd += evaluate(slides.get(slide1), slides.get(slide2 + 1)); //
            }
            pointToRemove += evaluate(slides.get(slide2), slides.get(slide2 + 1));
            pointToAdd += evaluate(slides.get(slide1), slides.get(slide2 + 1));
        }

        return pointToAdd - pointToRemove;
    }

    long delta(int slide1, int slide2) {
        //point when remove slide1
        long pointToRemove = 0;

        //point when add slide 2 to the position of slide1
        long pointToAdd = 0;

        if (slide1 > 0) {
            pointToRemove += evaluate(slides.get(slide1), slides.get(slide1 - 1));
            pointToAdd += evaluate(slides.get(slide2), slides.get(slide1 - 1));
        }
        if (slide1 < (slides.size() - 1)) {
            pointToRemove += evaluate(slides.get(slide1), slides.get(slide1 + 1));
            pointToAdd += evaluate(slides.get(slide2), slides.get(slide1 + 1));
        }
        if (slide2 > 0) {
            pointToRemove += evaluate(slides.get(slide2), slides.get(slide2 - 1));
            pointToAdd += evaluate(slides.get(slide1), slides.get(slide2 - 1));
        }
        if (slide2 < (slides.size() - 1)) {
            pointToRemove += evaluate(slides.get(slide2), slides.get(slide2 + 1));
            pointToAdd += evaluate(slides.get(slide1), slides.get(slide2 + 1));
        }

        return pointToAdd - pointToRemove;
    }

    int deltaSwapPhoto(int first, int second) {
        VerticalSlide slide1 = (VerticalSlide) slides.get(first);
        VerticalSlide slide2 = (VerticalSlide) slides.get(second);
        VerticalSlide newSlide1 = new VerticalSlide(slide2.photo2, slide1.photo2);
        VerticalSlide newSlide2 = new VerticalSlide(slide2.photo1, slide1.photo1);

        int slide1Before = 0;
        int slide2Before = 0;

        int slide1After = 0;
        int slide2After = 0;

        if (first > 0) {
            slide1Before += evaluate(slide1, slides.get(first - 1));
            slide1After += evaluate(newSlide1, slides.get(first - 1));
        }
        if (first < (slides.size() - 1)) {
            slide1Before += evaluate(slide1, slides.get(first + 1));
            slide1After += evaluate(newSlide1, slides.get(first + 1));
        }
        if (second > 0) {
            slide2Before += evaluate(slide2, slides.get(second - 1));
            slide2After += evaluate(newSlide2, slides.get(second - 1));
        }
        if (second < (slides.size() - 1)) {
            slide2Before += evaluate(slide2, slides.get(second + 1));
            slide2After += evaluate(newSlide2, slides.get(second + 1));
        }

        return slide1After + slide2After - slide1Before - slide2Before;
    }

    private void runCoreWithReverse() {
        // We have a solution
//        nextSlides = evaluateGlobal(slides);
        maximum.addAll(slides);

        int i = 0;
        while (i < globalInteration) {
            int j = 0;
            while (j < localInteration) {
//                nextSlides = evaluateGlobal(slides);

                // switch
                int first = random.nextInt(slides.size());
                int second = random.nextInt(slides.size());

                if (first > second) {
                    int temp = first;
                    first = second;
                    second = temp;
                }

                boolean vertical = slides.get(first).isVertical();
                boolean vertical1 = slides.get(second).isVertical();

                long delta;
                boolean verticalSwap = false;
                if (vertical && vertical1) {
                    delta = deltaSwapPhoto(first, second);
                    if (delta < 0) {
                        delta = deltaReverseRange(first, second);
                        verticalSwap = false;
                    } else {
                        verticalSwap = true;
                    }
                } else {
                    delta = deltaReverseRange(first, second);
                }
                if (delta > 0) {
                    // Keep the best
                    swapWithRangeReverse(first, second, vertical, vertical1, verticalSwap);
                    maximum.clear();
                    maximum.addAll(slides);
                } else if ((((300 + delta) / temperature) > random.nextFloat())) {
                    // switch slides
                    swapWithRangeReverse(first, second, vertical, vertical1, verticalSwap);
                }
                j++;
            }
            temperature = reductTempRatio * temperature;
            i++;
        }
        slides = maximum;
    }

    private void swapWithRangeReverse(int first, int second, boolean vertical, boolean vertical1, boolean verticalSwap) {
        if (vertical && vertical1 && verticalSwap) {
            VerticalSlide slide1 = (VerticalSlide) slides.get(first);
            VerticalSlide slide2 = (VerticalSlide) slides.get(second);
            VerticalSlide newSlide1 = new VerticalSlide(slide2.photo2, slide1.photo2);
            VerticalSlide newSlide2 = new VerticalSlide(slide2.photo1, slide1.photo1);
            slides.set(first, newSlide1);
            slides.set(second, newSlide2);
        } else {
            // swap range
            swapRange(first, second);
        }
    }

    private void swapRange(int first, int second) {
        List<Slide> slides = this.slides.subList(first, second);
        for (int i1 = 0; i1 < (slides.size() / 2); i1++) {
            swap2slides(i1, slides.size() - 1 - i1, slides);
        }
    }

    private void runCore() {
        // We have a solution
//        nextSlides = evaluateGlobal(slides);
        maximum.addAll(slides);

        int i = 0;
        while (i < globalInteration) {
            int j = 0;
            while (j < localInteration) {
//                nextSlides = evaluateGlobal(slides);

                // switch

                int first, second;
                do {
                    first = random.nextInt(slides.size());
                    second = random.nextInt(slides.size());
                } while (first == second);

                boolean vertical = slides.get(first).isVertical();
                boolean vertical1 = slides.get(second).isVertical();

                long delta;
                boolean verticalSwap = false;
                if (vertical && vertical1) {
                    delta = deltaSwapPhoto(first, second);
                    if (delta < 0) {
                        delta = delta(first, second);
                        verticalSwap = false;
                    } else {
                        verticalSwap = true;
                    }
                } else {
                    delta = delta(first, second);
                }
                if (delta > 0) {
                    // Keep the best
                    switchStandard(first, second, vertical, vertical1, verticalSwap);
                    maximum.clear();
                    maximum.addAll(slides);
                }
//                else if ((((300 + delta) / temperature) > random.nextFloat())) {
//                    // switch slides
//                    switchStandard(first, second, vertical, vertical1, verticalSwap);
//                }
                j++;
            }
            temperature = reductTempRatio * temperature;
            i++;
        }
        slides = maximum;
    }

    private void switchStandard(int first, int second, boolean vertical, boolean vertical1, boolean verticalSwap) {
        if (vertical && vertical1 && verticalSwap) {
            VerticalSlide slide1 = (VerticalSlide) slides.get(first);
            VerticalSlide slide2 = (VerticalSlide) slides.get(second);
            VerticalSlide newSlide1 = new VerticalSlide(slide2.photo2, slide1.photo2);
            VerticalSlide newSlide2 = new VerticalSlide(slide2.photo1, slide1.photo1);
            slides.set(first, newSlide1);
            slides.set(second, newSlide2);
        } else {
            swap2slides(first, second, slides);
        }
    }

    private void swap2slides(int first, int second, List<Slide> slides) {
        Slide firstSlide = slides.get(first);
        slides.set(first, slides.get(second));
        slides.set(second, firstSlide);
    }

}
