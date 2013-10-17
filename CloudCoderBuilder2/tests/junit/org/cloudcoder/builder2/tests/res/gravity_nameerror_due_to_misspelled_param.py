def gravity (time, initalVelocity):
    """
    return float indicating number of meters an object has fallen
    after being thrown with an initial velocity (given in meters per
    second) and after falling for time seconds
    """
    # TODO: complete code here
    vt = initialVelocity + 9.8 * time
    return (vt + initialVelocity) * time / 2