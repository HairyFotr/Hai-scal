package compiler;

public class Utils {

    public static int parseChar(String c) {
        int val = 0;
        if(c.length()>3) {
            if(c.charAt(c.length()-2)=='d') 
                val = Integer.parseInt(c.substring(1,c.length()-2));
            else if(c.charAt(c.length()-2)=='h') 
                val = Integer.parseInt(c.substring(1,c.length()-2), 16);
            else
                val = (int)c.charAt(1);                     
        } else {
            val = (int)c.charAt(1); 
        }
        return val;
    }


}
