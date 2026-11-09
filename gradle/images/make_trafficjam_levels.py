"""Makes the parkings of "Embouteillage" (Traffic Jam, name code RushHour; gazeplay-games: data/rushHour/levels.txt),
made for GazePlay Learning in November 2026 in place of 33 parkings of unknown source. Same license as GazePlay Learning
(GPL-3.0-or-later).

A parking is 6 x 6 boxes; the red car is on the third line and leaves by the right side. Random parkings are drawn,
then changed a little at a time (a vehicle removed, added or moved), a change being kept when the parking gets at least
as hard. For each parking, every position its vehicles can reach is explored, with the fewest moves to bring the red car
to the exit (a move: one vehicle slid by as many boxes as wanted). Are kept positions of each level, never solved, at
most 2 of a level by parking searched, spread over the range (at most 5, 4 and 5 of the same moves):
- easy: 3 to 8 moves; normal: 9 to 16 moves; hard: 17 to 30 moves;
30 parkings of each level, all different, the same at each run (fixed seed).

Each line of the file: <level> <moves> <36 characters, line by line from the top>, "." an empty box, "A" the red car, the
other letters the other vehicles (a vehicle of 2 boxes is a car, of 3 boxes a truck or a bus).

python make_trafficjam_levels.py <levels.txt>
"""
import random
import sys
from collections import deque

SIZE = 6
EXIT_LINE = 2
LEVELS = (('easy', 3, 8), ('normal', 9, 16), ('hard', 17, 30))
PER_LEVEL = 30
# At most this many parkings of a level with the same fewest moves, so that they spread over its range
CAP = {'easy': 5, 'normal': 4, 'hard': 5}


class Vehicle:
    def __init__(self, horizontal, length, fixed):
        # fixed: the line of a horizontal vehicle, the column of a vertical one
        self.horizontal = horizontal
        self.length = length
        self.fixed = fixed

    def cells(self, position):
        if self.horizontal:
            return [(self.fixed, position + k) for k in range(self.length)]
        return [(position + k, self.fixed) for k in range(self.length)]


def bits(vehicle, position):
    mask = 0
    for line, column in vehicle.cells(position):
        mask |= 1 << (line * SIZE + column)
    return mask


def draw_parking(rnd):
    """A random parking: the red car on the exit line, not at the exit, then other vehicles where they fit."""
    vehicles = [Vehicle(True, 2, EXIT_LINE)]
    positions = [rnd.randint(0, 3)]
    taken = bits(vehicles[0], positions[0])
    wanted = rnd.randint(8, 13)
    tries = 0
    while len(vehicles) < wanted and tries < 400:
        tries += 1
        horizontal = rnd.random() < 0.5
        length = 3 if rnd.random() < 0.25 else 2
        fixed = rnd.randrange(SIZE)
        if horizontal and fixed == EXIT_LINE:
            # Another vehicle lying on the exit line could never let the red car out
            continue
        vehicle = Vehicle(horizontal, length, fixed)
        position = rnd.randint(0, SIZE - length)
        mask = bits(vehicle, position)
        if mask & taken:
            continue
        vehicles.append(vehicle)
        positions.append(position)
        taken |= mask
    return vehicles, tuple(positions)


def moves_from(vehicles, state):
    """The positions reached by sliding one vehicle by one box or more."""
    taken = 0
    for vehicle, position in zip(vehicles, state):
        taken |= bits(vehicle, position)
    for index, (vehicle, position) in enumerate(zip(vehicles, state)):
        free = taken & ~bits(vehicle, position)
        for way in (-1, 1):
            moved = position + way
            while 0 <= moved <= SIZE - vehicle.length and not (bits(vehicle, moved) & free):
                yield state[:index] + (moved,) + state[index + 1:]
                moved += way


def solved(state):
    return state[0] == SIZE - 2


def explore(vehicles, start, limit=60000):
    """Every position reachable from the start, and the fewest moves from each one to the exit (None if unsolvable or
    too large)."""
    neighbors = {}
    queue = deque([start])
    neighbors[start] = None
    while queue:
        state = queue.popleft()
        following = list(moves_from(vehicles, state))
        neighbors[state] = following
        for other in following:
            if other not in neighbors:
                neighbors[other] = None
                queue.append(other)
                if len(neighbors) > limit:
                    return None
    goals = [state for state in neighbors if solved(state)]
    if not goals:
        return None
    distance = {state: 0 for state in goals}
    queue = deque(goals)
    while queue:
        state = queue.popleft()
        for other in neighbors[state]:
            if other not in distance:
                distance[other] = distance[state] + 1
                queue.append(other)
    return distance


def text(vehicles, state):
    grid = ['.'] * (SIZE * SIZE)
    for index, (vehicle, position) in enumerate(zip(vehicles, state)):
        for line, column in vehicle.cells(position):
            grid[line * SIZE + column] = chr(ord('A') + index)
    return ''.join(grid)


def mutate(rnd, vehicles, state):
    """A parking a little different: a vehicle (never the red car) removed, added, or moved elsewhere."""
    vehicles = list(vehicles)
    state = list(state)
    action = rnd.random()
    if action < 0.3 and len(vehicles) > 6:
        index = rnd.randrange(1, len(vehicles))
        del vehicles[index]
        del state[index]
        return vehicles, tuple(state)
    if action < 0.65:
        index = rnd.randrange(1, len(vehicles))
        del vehicles[index]
        del state[index]
    taken = 0
    for vehicle, position in zip(vehicles, state):
        taken |= bits(vehicle, position)
    for _ in range(100):
        horizontal = rnd.random() < 0.5
        length = 3 if rnd.random() < 0.3 else 2
        fixed = rnd.randrange(SIZE)
        if horizontal and fixed == EXIT_LINE:
            continue
        vehicle = Vehicle(horizontal, length, fixed)
        position = rnd.randint(0, SIZE - length)
        if not bits(vehicle, position) & taken:
            vehicles.append(vehicle)
            state.append(position)
            break
    return vehicles, tuple(state)


def main():
    rnd = random.Random(20261109)
    found = {name: {} for name, _, _ in LEVELS}

    kept = {}

    def keep(vehicles, distance):
        for name, low, high in LEVELS:
            # At most 2 parkings of a level by climb, so that they are not alike
            if len(found[name]) >= PER_LEVEL or kept.get(name, 0) >= 2:
                continue
            # Spread over the range: at most CAP[name] parkings of the same number of moves, the rarest first
            counts = {}
            for value in found[name].values():
                counts[value] = counts.get(value, 0) + 1
            moves = sorted({m for m in distance.values() if low <= m <= high and counts.get(m, 0) < CAP[name]})
            if not moves:
                continue
            fewest = min(counts.get(m, 0) for m in moves)
            wanted = rnd.choice([m for m in moves if counts.get(m, 0) == fewest])
            choice = rnd.choice(sorted(state for state, m in distance.items() if m == wanted))
            grid = text(vehicles, choice)
            if grid not in found[name]:
                found[name][grid] = wanted
                kept[name] = kept.get(name, 0) + 1

    climbs = 0
    while any(len(found[name]) < PER_LEVEL for name, _, _ in LEVELS) and climbs < 2000:
        climbs += 1
        kept.clear()
        vehicles, start = draw_parking(rnd)
        distance = explore(vehicles, start, 30000)
        if distance is None:
            continue
        keep(vehicles, distance)
        hardest = max(distance.values())
        state = max(distance, key=lambda s: (distance[s], s))
        # Climb: a change is kept when the parking gets at least as hard
        for _ in range(120 if len(found['hard']) < PER_LEVEL else 10):
            other_vehicles, other_start = mutate(rnd, vehicles, state)
            other = explore(other_vehicles, other_start, 30000)
            if other is None:
                continue
            other_hardest = max(other.values())
            if other_hardest >= hardest:
                vehicles, hardest = other_vehicles, other_hardest
                distance = other
                state = max(distance, key=lambda s: (distance[s], s))
                keep(vehicles, distance)
            if hardest > 30:
                break
        print(climbs, hardest, {name: len(found[name]) for name in found}, flush=True)
    with open(sys.argv[1], 'w', encoding='utf-8', newline='\n') as file:
        file.write('# Parkings of "Embouteillage", made by gradle/images/make_trafficjam_levels.py: '
                   '<level> <fewest moves> <36 boxes, line by line from the top>\n')
        for name, _, _ in LEVELS:
            for grid, moves in sorted(found[name].items(), key=lambda item: (item[1], item[0])):
                file.write(f'{name} {moves} {grid}\n')
    print({name: len(found[name]) for name in found}, 'climbs', climbs)


main()
