import random
for i in range(17):
    with open(f"input{i+1}.txt", "w") as f:
        nums = [str(random.randint(-100,100)) for x  in range(100)]
        print(" ".join(nums))
        f.write(" ".join(nums))
