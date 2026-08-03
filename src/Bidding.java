import java.util.HashMap;
import java.util.Scanner;
public class Bidding {
public Bidding (){
}

public static int Quantify (String suite, String number){
    //looking at th esuite and the number evaluate ranking

    return 0;
}
    public static void main(String[] args) {

        //hashmap for lookup of cardinal position
        HashMap<Integer, Character> positions = new HashMap<>();
        positions.put(1, 'N');
        positions.put(2, 'E');
        positions.put(3, 'S');
        positions.put(4, 'W');
        //Never Eat Silk Worms
        //1:N, 2:E, 3:S, 4:W

        int highestbid = 0;
        Scanner sc = new Scanner(System.in);
        int passes = 0;//counter for # of current passes
        int hand = 0;
        int bid = 0;
        while (passes != 3) {
            //When further in development, it wont be a prompt, while tash is doing the classes she has to do, i will make a more basic version of bidding that doesn't require a GUI or the classes yet to be made
            System.out.println("Please submit your bid, Enter the suite: (C, D, H, S, NT) -- press Enter to pass");
            String st = sc.nextLine();
            if (!st.isEmpty()){
                passes++;
                hand = (hand + 1) % 4;
                continue;
            };
            passes = 0;
            System.out.println("Enter the value (2 -A)");
            String val = sc.nextLine();
            bid = Quantify(st, val);
            if ( bid > highestbid){
                highestbid = bid;
            };
            //look at value of the bid, and at the same time not allow any bids lower than this

            }


        }
    }


