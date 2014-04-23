package net.whydah.admin;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class ConflictExeption extends RuntimeException {
    public ConflictExeption(String s) {
        super(s);
    }
}
