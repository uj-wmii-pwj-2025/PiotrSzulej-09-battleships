package main.uj.wmii.pwj.battleships;

import java.util.Random;

class Creator implements BattleshipGenerator
{
    String result;
    char[][] board;
    int[][] occupied;
    Random rand = new Random();
    public Creator()
    {
        result = "";
        board = new char[10][10];
        occupied = new int[10][10];
    }
    @Override
    public String generateMap()
    {
        result = "";
        for(int i = 0; i < 10; i++)
        {
            for(int j = 0; j < 10; j++)
            {
                board[i][j] = '.';
                occupied[i][j] = 0;
            }
        }

        createShip(board, occupied, 4);
        createShip(board, occupied, 3);
        createShip(board, occupied, 3);
        createShip(board, occupied, 2);
        createShip(board, occupied, 2);
        createShip(board, occupied, 2);
        createShip(board, occupied, 1);
        createShip(board, occupied, 1);
        createShip(board, occupied, 1);
        createShip(board, occupied, 1);

        for(int i = 0; i < 10; i++)
        {
            for(int j = 0; j < 10; j++)
            {
                result += board[i][j];
            }
        }
        return result;
    }

    public void createShip(char[][] board, int[][] occupied, int length)
    {
        if (length == 1)
        {
            while (true)
            {
                int p = rand.nextInt(10);
                int q = rand.nextInt(10);

                if (occupied[p][q] == 0)
                {
                    board[p][q] = '#';

                    for (int i = -1; i <= 1; i++)
                    {
                        for (int j = -1; j <= 1; j++)
                        {
                            int x = p + i, y = q + j;
                            if (x >= 0 && x < 10 && y >= 0 && y < 10)
                            {
                                occupied[x][y] = 1;
                            }
                        }
                    }
                    return;
                }
            }
        }
        else
        {
            int[] directions = {0, 1, 2, 3};
            permutation(directions);

            for (int p = 0; p < 10; p++)
            {
                for (int q = 0; q < 10; q++)
                {
                    if (occupied[p][q] != 1)
                    {
                        for (int k = 0; k < 4; k++)
                        {

                            int direction = directions[k];
                            int dp = 0, dq = 0;

                            switch (direction)
                            {
                                case 0:
                                    dp = 1;
                                    dq = 0;
                                    break;
                                case 1:
                                    dp = -1;
                                    dq = 0;
                                    break;
                                case 2:
                                    dp = 0;
                                    dq = 1;
                                    break;
                                case 3:
                                    dp = 0;
                                    dq = -1;
                                    break;
                            }

                            int endP = p + dp * (length - 1);
                            int endQ = q + dq * (length - 1);

                            if (endP < 0 || endP > 9 || endQ < 0 || endQ > 9)
                            {
                                continue;
                            }

                            boolean collision = false;

                            for (int i = -1; i <= length; i++)
                            {
                                for (int j = -1; j <= 1; j++)
                                {

                                    int x = p + i * dp + j * dq;
                                    int y = q + i * dq + j * dp;

                                    if (x < 0 || x > 9 || y < 0 || y > 9)
                                    {
                                        continue;
                                    }

                                    if (occupied[x][y] == 1)
                                    {
                                        collision = true;
                                        break;
                                    }
                                }
                                if(collision)
                                {
                                    break;
                                }
                            }

                            if (collision)
                            {
                                continue;
                            }

                            for (int i = 0; i < length; i++)
                            {
                                int x = p + i * dp;
                                int y = q + i * dq;
                                board[x][y] = '#';
                            }

                            for (int i = -1; i <= length; i++)
                            {
                                for (int j = -1; j <= 1; j++)
                                {
                                    int x = p + i * dp + j * dq;
                                    int y = q + i * dq + j * dp;
                                    if (x >= 0 && x < 10 && y >= 0 && y < 10)
                                        occupied[x][y] = 1;
                                }
                            }

                            return;
                        }
                    }
                }
            }
        }
    }

    public void permutation (int[] array)
    {
        for (int i = array.length - 1; i > 0; i--)
        {
            int j = rand.nextInt(i + 1);
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }
}