class Snake {
  constructor() {
    this.body = [];
    this.body[0] = createVector(floor(w/2), floor(h/2));
    this.xdir = 0;
    this.ydir = 0;
  }
  
  setDir(x,y) {
    this.xdir = x;
    this.ydir = y;
  }
  
  eat(pos) {
    if(this.body[this.body.length-1].x == pos.x && this.body[this.body.length-1].y == pos.y)  {
      this.grow();
      return true;
    }
    return false;
  }
  
  isDead() {
    let x = this.body[this.body.length-1].x;
    let y = this.body[this.body.length-1].y;
    if(x<0 || x>w-1 || y<0 || y>h-1) {
      return true;
    }
    for(let i=0;i<this.body.length-1;i++) {
      if(x == this.body[i].x && y == this.body[i].y) {
        return true;
      }
    }
    return false;
  }
  
  grow() {
    this.body.push(this.body[this.body.length-1].copy());
  }
  update() {
    let head = this.body[this.body.length-1].copy();
    this.body.shift();
    head.x += this.xdir;
    head.y += this.ydir;
    this.body.push(head);
  }
  
  show() {
    for(let i=0;i<this.body.length;i++) {
      fill(0);
      noStroke();
      rect(this.body[i].x, this.body[i].y, 1, 1);
    }
  }
}
