/*
*   public BinaryTree<E> copyToDepth(int d) {
        if (d < 0) {
            return null; // return null if the depth is negative
        }
        else if (d == 0) {
            return new BinaryTree<>(this.data); // return a new tree with the current node's data and no children if the depth is 0
        }
        else {
            BinaryTree<E> leftCopy = null;
            BinaryTree<E> rightCopy = null;

            if (this.hasLeft()) {
                leftCopy = this.left.copyToDepth(d - 1); // recursively copy the left subtree to the depth of d-1
            }

            if (this.hasRight()) {
                rightCopy = this.right.copyToDepth(d - 1); // recursively copy the right subtree to the depth of d-1
            }

            return new BinaryTree<>(this.data, leftCopy, rightCopy); // return a new tree with the current node's data and the copied left and right subtrees
        }
    }
*  */