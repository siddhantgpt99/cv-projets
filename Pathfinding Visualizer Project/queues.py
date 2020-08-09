import heapq

class PriorityQueue:

    """
    Priority queue. Implemented as a heapq from collections.
    Used in Dijkstra and A*.
    """

    def __init__(self):
        self.elements = []

    def empty(self):
        return len(self.elements) == 0

    def put(self, item, priority):
        heapq.heappush(self.elements, (priority, item))

    def pop(self):
        return heapq.heappop(self.elements)[1]

    def __len__(self):
        return len(self.elements)
