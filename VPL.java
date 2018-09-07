package project1;

/*
Elie S.

He said he just needs our VPL.java.
Whoever's last to review should turn it in too.
*/
import java.io.*;
import java.util.*;

public class VPL {
    private static boolean debug = true;
    static String fileName;
    static Scanner keys;

    static int max;
    static Integer[] mem;
    static int ip, bp, sp, rv, hp, numPassed, gp;
    static int step;

    public static void main(String[] args) throws Exception {

        keys = new Scanner(System.in);

        if (args.length != 2) {
            System.out.println("Usage: java VPL <vpl program> <memory size>");
            System.exit(1);
        }

        fileName = args[0];

        max = Integer.parseInt(args[1]);
        mem = new Integer[max];

        // load the program into the front part of
        // memory
        Scanner input = new Scanner(new File(fileName));
        String line;
        StringTokenizer st;
        int opcode;

        ArrayList<IntPair> labels, holes;
        labels = new ArrayList<IntPair>();
        holes = new ArrayList<IntPair>();
        int label;

        // load the code

        int k = 0;
        while (input.hasNextLine()) {
            line = input.nextLine();
            System.out.println("parsing line [" + line + "]");
            if (line != null) {// extract any tokens
                st = new StringTokenizer(line);
                if (st.countTokens() > 0) {// have a token, so must be an instruction (as opposed to empty line)

                    opcode = Integer.parseInt(st.nextToken());

                    // load the instruction into memory:

                    if (opcode == labelCode) {// note index that comes where label would go
                        label = Integer.parseInt(st.nextToken());
                        labels.add(new IntPair(label, k));
                    }
                    else if (opcode == noopCode) {
                    }
                    else {// opcode actually gets stored
                        mem[k] = opcode;
                        k++;

                        if (opcode == callCode || opcode == jumpCode || opcode == condJumpCode) {// note the hole immediately after the opcode to be filled in later
                            label = Integer.parseInt(st.nextToken());
                            mem[k] = label;
                            holes.add(new IntPair(k, label));
                            ++k;
                        }

                        // load correct number of arguments (following label, if any):
                        for (int j = 0; j < numArgs(opcode); ++j) {
                            mem[k] = Integer.parseInt(st.nextToken());
                            ++k;
                        }

                    }// not a label

                }// have a token, so must be an instruction
            }// have a line
        }// loop to load code

        //System.out.println("after first scan:");
        //showMem( 0, k-1 );

        // fill in all the holes:
        int index;
        for (int m = 0; m < holes.size(); ++m) {
            label = holes.get(m).second;
            index = -1;
            for (int n = 0; n < labels.size(); ++n)
                if (labels.get(n).first == label) {
                    index = labels.get(n).second;
                }
            mem[holes.get(m).first] = index;
        }

        System.out.println("after replacing labels:");
        showMem(0, k - 1);

        // initialize registers:
        bp = k;
        sp = k + 2;
        ip = 0;
        rv = -1;
        hp = max;
        numPassed = 0;

        int codeEnd = bp - 1;

        System.out.println("Code is ");
        showMem(0, codeEnd);

        gp = codeEnd + 1;

        // start execution:
        boolean done = false;
        int op, a = 0, b = 0, c = 0;
        int actualNumArgs;

        int step = 0;

        int oldIp = 0;

        // repeatedly execute a single operation
        // *****************************************************************

        do {
            // show details of current step
            System.out.println("--------------------------");
            System.out.println("Step of execution with IP = " + ip + " opcode: " + mem[ip] + " bp = " + bp + " sp = " + sp + " hp = " + hp + " rv = " + rv);
            System.out.println(" chunk of code: " + mem[ip] + " " + mem[ip + 1] + " " + mem[ip + 2] + " " + mem[ip + 3]);
            System.out.println("--------------------------");
/*
            System.out.println(" memory from " + (codeEnd + 1) + " up: ");
            showMem(codeEnd + 1, sp + 3);

*/
            System.out.println("hit <enter> to go on");
            keys.nextLine();

            oldIp = ip;

            op = mem[ip];
            ip++;
            // extract the args into a, b, c for convenience:
            a = -1;
            b = -2;
            c = -3;

            // numArgs is wrong for these guys, need one more!
            if (op == callCode || op == jumpCode || op == condJumpCode) {
                actualNumArgs = numArgs(op) + 1;
            }
            else {
                actualNumArgs = numArgs(op);
            }

            if (actualNumArgs == 1) {
                a = mem[ip];
                ip++;
            }
            else if (actualNumArgs == 2) {
                a = mem[ip];
                ip++;
                b = mem[ip];
                ip++;
            }
            else if (actualNumArgs == 3) {
                a = mem[ip];
                ip++;
                b = mem[ip];
                ip++;
                c = mem[ip];
                ip++;
            }

            // implement all operations here:
            // ********************************************
            String message;
            // put your work right here!
            if (op == callCode) { //2
            /*
            To call a subprogram, first we have to use command 3 once for each value that we want to
            pass over to the next stack frame. Then we use command 2 to call the desired subprogram.
            This command stores the correct values of the current base pointer and instruction pointer
            in the first two cells of the new stack frame. Note that the arguments passed by command
            3 sit just to the right of those two cells.
            And, command 2 has to change the instruction pointer to the starting point in the code
            segment of the subprogram being called.

            2L
            Do all the steps necessary to set up for execution
            of the subprogram that begins at label L
             */
                message = "Calling " + a;
                final int newIP = a;
                Runnable copyCurrentBP = () -> mem[sp++] = bp;
                Runnable copyCurrentIP = () -> mem[sp++] = ip;
                Runnable moveBP = () -> bp = sp - 2;
                Runnable moveSP = () -> sp += numPassed;
                Runnable moveIP = () -> ip = newIP;
                Runnable resetNumPassed = () -> numPassed = 0;
                doOperations(codeEnd, sp + numPassed + 5, message, copyCurrentBP, copyCurrentIP, moveBP, moveSP, moveIP, resetNumPassed);
            }
            else if (op == passCode) {//3
                /*
                3 a
                Push the contents of cell a on the stack
                 */
                final int content = getLocalContentOf(a);
                message = "Pushing the contents of cell " + a + " on the stack.";
                doOperations(codeEnd, sp + 2 + numPassed + 1, message, () -> mem[sp + 2 + numPassed++] = content);
            }

            else if (op == allocCode) { //4
                /*
                Increase sp by n to make space for local variables in the current stack frame.
                 */
                final int spotsToAllocateLocally = a;
                message = "allocated " + spotsToAllocateLocally + " local cell(s).";
                doOperations(codeEnd, sp + spotsToAllocateLocally, message, () -> sp += spotsToAllocateLocally);
            }
            else if (op == returnCode) {//5
                /*
                 When command 5 executes, it puts the value to be returned in the return value register,
                and then restores the base pointer, the stack pointer, and the instruction pointer, thus
                returning both in code and in the stack to the stack frame from which the call was made.
                Then in the calling stack frame, command 6 moves the returned value into a local cell.
                5 a
                Do all the steps necessary to return from the current subprogram,
                including putting the value stored in cell a in rv
                 */
                final int newRV = getLocalContentOf(a);
                Runnable storeRV = () -> rv = newRV;
                Runnable restoreSP = () -> sp = bp;
                Runnable restoreIP = () -> ip = mem[bp + 1];
                Runnable restoreBP = () -> bp = mem[bp];

                message = "Storing " + newRV + " into the rv register.";
                doOperations(codeEnd, sp, message, storeRV, restoreSP, restoreIP, restoreBP);
            }
            else if (op == getRetvalCode) {//6
                /*
                6 a
                Copy the value stored in rv into cell a.
                 */
                message = "Copying " + rv + " into cell " + (bp + 2 + a);
                final int A = a;
                doOperations(codeEnd, sp, message, () -> mem[bp + 2 + A] = rv);
            }
            else if (op == jumpCode) {//7
                final int newIP = a;
                message = "Jumping to " + newIP;
                doOperations(ip, sp, message, () -> ip = newIP);
            }
            else if (op == condJumpCode) {//8
                /*
                8 L a
                If the value stored in cell a is non-zero, change ip to L (otherwise, move ip to the next instruction).
                 */
                final boolean jump = getLocalContentOf(b) != 0;
                final int newIP = a;
                if (jump) {
                    message = "Conditionally Jumping to " + newIP;
                    doOperations(ip, sp, message, () -> ip = newIP);
                }
                else {
                    message = "NOT conditionally jumping to " + newIP;
                    doOperations(ip, sp, message, () -> {
                    });
                }
            }
            else if (op == addCode) {//9
                /*
                9 a b c
                Add the values in cell b and cell c and store the result in cell a
                 */
                math(a, b, c, codeEnd, "Adding ", (op1, op2) -> op1 + op2);
            }
            else if (op == subCode) {//10
                math(a, b, c, codeEnd, "Subtracting ", (op1, op2) -> op1 - op2);
            }
            else if (op == multCode) {//11
                math(a, b, c, codeEnd, "Multiplying ", (op1, op2) -> op1 * op2);
            }
            else if (op == divCode) {//12
                math(a, b, c, codeEnd, "Dividing ", (op1, op2) -> op1 / op2);
            }
            else if (op == remCode) {//13
                math(a, b, c, codeEnd, "Mod-ing ", (op1, op2) -> op1 % op2);
            }
            else if (op == equalCode) {//14
                math(a, b, c, codeEnd, "Equal to ", (op1, op2) -> op1 == op2 ? 1 : 0);
            }
            else if (op == notEqualCode) {//15
                math(a, b, c, codeEnd, "Not Equal to ", (op1, op2) -> op1 != op2 ? 1 : 0);
            }
            else if (op == lessCode) {//16
                math(a, b, c, codeEnd, "Less than ", (op1, op2) -> op1 < op2 ? 1 : 0);
            }
            else if (op == lessEqualCode) {//17
                math(a, b, c, codeEnd, "Less than or equal to ", (op1, op2) -> op1 <= op2 ? 1 : 0);
            }
            else if (op == andCode) {//18
                math(a, b, c, codeEnd, "Anding ", (op1, op2) -> (op1 != 0 && op2 != 0) ? 1 : 0);
            }
            else if (op == orCode) {//19
                math(a, b, c, codeEnd, "Oring  ", (op1, op2) -> (op1 != 0 || op2 != 0) ? 1 : 0);
            }
            else if (op == notCode) {//20
                final int bContents = getLocalContentOf(b);
                final int aIndex = bp + 2 + a;
                message = "Notting " + bContents;
                doOperations(codeEnd, sp, message, () -> mem[aIndex] = bContents == 0 ? 1 : 0);
            }


            else if (op == oppCode) { //21
                mem[bp + 2 + a] = -mem[bp + 2 + b];
            }
            else if (op == litCode) { //22
                /*
                22 a n
                Put n in cell a.
                 */
                final int n = b;
                final int A = a;

                message = "Putting " + n + " in local cell " + A;
                doOperations(codeEnd, sp, message, () -> mem[bp + 2 + A] = n);
            }
            else if (op == copyCode) {
                //23 a b
//                Copy the value in cell b into cell A
                final int aContents = getLocalContentOf(a);
                final int B = b;

                message = "Copying " + aContents + " to " + bp + 2 + b;
                doOperations(codeEnd, sp, message, () -> mem[bp + 2 + B] = aContents);
            }
            else if (op == getCode) {//24
                /*
                24 a b c
                Get the value stored in the heap at the index
                obtained by adding the value of cell b and the
                value of cell c and copy it into cell a.
                 */
                final int heapIndex = hp + b + c;
                final int A = a;
                message = "local index " + mem[bp + 2 + A] + " = " + mem[heapIndex];
                doOperations(codeEnd, sp, message, () -> mem[bp + 2 + A] = mem[heapIndex]);
            }
            else if (op == putCode) {//25
                /*
                25 a b c
                Take the value from cell c and store it in the heap
                at the location with index computed as the value
                in cell a plus the value in cell b.
                 */

                final int B = b;
                final int A = a;
                final int cContents = getLocalContentOf(c);
                message = "Putting " + cContents + " into hp[" + (a + b) + "]";
                doOperations(codeEnd, sp, message, () -> mem[A + B] = cContents);
            }

            else if (op == haltCode) {//26
                done = true;
            }
            else if (op == inputCode) {//27
                /*
                27 a
                Print a ? and a space in the console and wait for an integer value to be typed by the user, and then store it in cell a
                 */
                System.out.print("? ");
                final int userInput = keys.nextInt();
                final int A = a;
                message = "Storing " + userInput + " in LOCAL cell " + a;
                doOperations(codeEnd, sp, message, () -> mem[bp + 2 + A] = userInput);
            }
            else if (op == outputCode) {//28
                /*
                28 a
                Display the value stored in cell a in the console
                 */
                final int aContents = getLocalContentOf(a);
                message = "Displaying the value stored in cell " + a + " to the console.";
                doOperations(codeEnd, sp, message, () -> System.out.print(aContents));
            }
            else if (op == newlineCode) {//29
                /*
                29 newline
                Move the console cursor to the beginning of the next line
                 */
                System.out.println();
            }

            else if (op == newCode) {//31
                /*
                31 a b
                Let the value stored in cell b be denoted by m.
                Decrease hp by m and put the new value of hp in cell a.
                 */
                final int m = getLocalContentOf(b);
                final int A = a;
                message = "New command. Decreasing hp by " + m;
                message += "\ncell " + (bp + 2 + A) + " now holds " + (hp - m);
                message += "\nold hp = " + hp + "\tnew hp = " + (hp - m);

                Runnable decreaseHP = () -> hp -= m;
                Runnable storeHP = () -> mem[bp + 2 + A] = hp;
                doOperations(codeEnd, sp, message, decreaseHP, storeHP);
            }
            else if (op == allocGlobalCode) { //32
                /*
                This instruction must occur first in any program that uses it. It simply sets the initial value of
                sp to n cells beyond the end of stored program memory, and sets gp to the end of stored program memory
                 */

                final int spotsToAllocateGlobally = a;
                Runnable changeSP = () -> sp += spotsToAllocateGlobally;
                Runnable changeBP = () -> bp += spotsToAllocateGlobally;

                message = "allocated " + spotsToAllocateGlobally + " global cell(s).";

                doOperations(codeEnd, sp + spotsToAllocateGlobally, message, changeSP, changeBP);
            }
            else if (op == toGlobalCode) { // 33
                /*
                33 n a
                Copy the contents of cell a to the global memory area at index gp+n.
                 */
                final int A = b;
                final int n = a;

                message = "Copying contents of LOCAL cell " + A + " to GLOBAL index " + n;
                message += "\n\t Copying " + mem[bp + 2 + A] + " to cell " + (gp + n);
                doOperations(codeEnd, sp, message, () -> mem[gp + n] = mem[bp + 2 + A]);
            }
            else if (op == fromGlobalCode) {//34
                /*
                34 a n
                Copy the contents of the global memory cell at index gp+n into cell a.*/
                final int contents = mem[gp + b];
                final int A = a;

                message = "Copying " + contents + " to LOCAL cell " + A;
                doOperations(codeEnd, sp, message, () -> mem[bp + 2 + A] = contents);

            }
            else {
                System.out.println("Fatal error: unknown opcode [" + op + "]");
                System.exit(1);
            }
            step++;
        } while (!done);


    }// main

    // use symbolic names for all opcodes:

    // op to produce comment
    private static final int noopCode = 0;

    // ops involved with registers
    private static final int labelCode = 1;
    private static final int callCode = 2;
    private static final int passCode = 3;
    private static final int allocCode = 4;
    private static final int returnCode = 5;  // return a means "return and put
    // copy of value stored in cell a in register rv
    private static final int getRetvalCode = 6;//op a means "copy rv into cell a"
    private static final int jumpCode = 7;
    private static final int condJumpCode = 8;

    // arithmetic ops
    private static final int addCode = 9;
    private static final int subCode = 10;
    private static final int multCode = 11;
    private static final int divCode = 12;
    private static final int remCode = 13;
    private static final int equalCode = 14;
    private static final int notEqualCode = 15;
    private static final int lessCode = 16;
    private static final int lessEqualCode = 17;
    private static final int andCode = 18;
    private static final int orCode = 19;
    private static final int notCode = 20;
    private static final int oppCode = 21;

    // ops involving transfer of data
    private static final int litCode = 22;  // litCode a b means "cell a gets b"
    private static final int copyCode = 23;// copy a b means "cell a gets cell b"
    private static final int getCode = 24; // op a b means "cell a gets
    // contents of cell whose
    // index is stored in b"
    private static final int putCode = 25;  // op a b means "put contents
    // of cell b in cell whose offset is stored in cell a"

    // system-level ops:
    private static final int haltCode = 26;
    private static final int inputCode = 27;
    private static final int outputCode = 28;
    private static final int newlineCode = 29;
    private static final int symbolCode = 30;
    private static final int newCode = 31;

    // global variable ops:
    private static final int allocGlobalCode = 32;
    private static final int toGlobalCode = 33;
    private static final int fromGlobalCode = 34;

    // debug ops:
    private static final int debugCode = 35;

    // return the number of arguments after the opcode,
    // except ops that have a label return number of arguments
    // after the label, which always comes immediately after
    // the opcode
    private static int numArgs(int opcode) {
        // highlight specially behaving operations
        if (opcode == labelCode) {
            return 1;  // not used
        }
        else if (opcode == jumpCode) {
            return 0;  // jump label
        }
        else if (opcode == condJumpCode) {
            return 1;  // condJump label expr
        }
        else if (opcode == callCode) {
            return 0;  // call label
        }

        // for all other ops, lump by count:

        else if (opcode == noopCode || opcode == haltCode || opcode == newlineCode || opcode == debugCode) {
            return 0;  // op
        }
        else if (opcode == passCode || opcode == allocCode || opcode == returnCode || opcode == getRetvalCode || opcode == inputCode || opcode == outputCode || opcode == symbolCode || opcode == allocGlobalCode) {
            return 1;  // op arg1
        }
        else if (opcode == notCode || opcode == oppCode || opcode == litCode || opcode == copyCode || opcode == newCode || opcode == toGlobalCode || opcode == fromGlobalCode

        ) {
            return 2;  // op arg1 arg2
        }
        else if (opcode == addCode || opcode == subCode || opcode == multCode || opcode == divCode || opcode == remCode || opcode == equalCode || opcode == notEqualCode || opcode == lessCode || opcode == lessEqualCode || opcode == andCode || opcode == orCode || opcode == getCode || opcode == putCode) {
            return 3;
        }
        else {
            System.out.println("Fatal error: unknown opcode [" + opcode + "]");
            System.exit(1);
            return -1;
        }

    }// numArgs

    private static void math(int a, int b, int c, int codeEnd, String message, Math operation) throws Exception {
        /*
                9 a b c
                Add the values in cell b and cell c and store the result in cell a
         */

        final int bContents = getLocalContentOf(b);
        final int cContents = getLocalContentOf(c);
        final int aIndex = bp + 2 + a;
        final Integer result = operation.apply(bContents, cContents);

        message += bContents + " and " + cContents + " = " + result + ". Cell " + aIndex + " contains " + result;
        doOperations(codeEnd, sp, message, () -> mem[aIndex] = result);
    }

    private static void doOperations(int a, int b, String message, Runnable... functions) throws Exception {
        ArrayList<String> before = showMemory(a, b);
        List.of(functions).forEach(e -> e.run());
        ArrayList<String> after = showMemory(a, b);

        if (debug) {
            //print it nicely
            Integer maxBefore = before.stream().map(e -> e.length()).max(Integer::compareTo).get();
            Integer maxAfter = after.stream().map(e -> e.length()).max(Integer::compareTo).get();
            String formatBefore = "%-" + maxBefore * 2 + "s";
            String formatAfter = "%-" + maxAfter * 2 + "s";

            for (int i = 0; i < before.size(); i++) {
                System.out.println(String.format(formatBefore, before.get(i)) + String.format(formatAfter, after.get(i)));
            }
            System.out.println("\t" + message);
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
    }

    private static ArrayList<String> showMemory(int a, int b) {
        ArrayList<String> messages = new ArrayList<>();
        for (int k = a; k <= b; ++k) {
//                static int ip, bp, sp, rv, hp, numPassed, gp;
            String message = k + ": " + mem[k];
            if (gp == k) {
                message += " GP";
            }
            if (sp == k) {
                message += " SP";
            }
            if (ip == k) {
                message += " IP";
            }
            if (hp == k) {
                message += " HP";
            }
            if (bp == k) {
                message += " BP";
            }
            messages.add(message);
        }
        return messages;
    }

    private static void showMem(int a, int b) {
        for (int k = a; k <= b; ++k) {
            System.out.println(k + ": " + mem[k]);
        }
    }// showMem

    private static int getLocalContentOf(int index) {
        return mem[bp + 2 + index];
    }

    interface Math {
        Integer apply(Integer op1, Integer op2);
    }

}// VPL
