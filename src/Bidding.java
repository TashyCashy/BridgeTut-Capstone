import java.util.Scanner;
public class Bidding

public static void main(String [] args){
    //1 is north
    int [] players =  {1,2,3,4}; //Never Eat Silk Worms
    String highestbid = "";
    Scanner sc = new Scanner(System.in);
    // we go clockwise so if we were to flatten the circle it would eb an array going from left to right
    int passes = 0; //counter for # of current passes
    while (passes != 3) {
        for (int i =0; i< players.length; i++){
            System.out.println("Please submit your bid: # "); // number for now
            sc.nextLine();

        }
    }

}
