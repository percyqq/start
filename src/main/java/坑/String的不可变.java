package 坑;

/**
 * @description:
 * @create: 2020-12-24 10:25
 */
public class String的不可变 {

    public static void main(String[] args) {


        String wtf = " 135 is 2b  ";
        System.out.println(wtf.trim());
        System.out.println(wtf);

        // 注意要设值！！
        wtf = wtf.trim();
    }
}
